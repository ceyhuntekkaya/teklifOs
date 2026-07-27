from contextlib import asynccontextmanager

import uvicorn
from fastapi import FastAPI
from teklifos_shared.logging import configure_logging, get_logger
from teklifos_shared.middleware import CorrelationIdMiddleware
from teklifos_shared.settings import ServiceSettings

from document_service.consumer import start_consumer
from document_service.routers import upload

DEFAULT_PORT = 9002


@asynccontextmanager
async def lifespan(app: FastAPI):
    settings: ServiceSettings = app.state.settings
    start_consumer(settings)
    yield


def create_app(settings: ServiceSettings | None = None) -> FastAPI:
    cfg = settings or ServiceSettings(service_name="document-service", port=DEFAULT_PORT)
    configure_logging(service_name=cfg.service_name, log_level=cfg.log_level)
    log = get_logger(__name__)

    app = FastAPI(title="Document Service", version="0.1.0", lifespan=lifespan)
    app.add_middleware(CorrelationIdMiddleware)
    app.state.settings = cfg

    @app.get("/health")
    async def health() -> dict[str, str]:
        return {"status": "ok", "service": cfg.service_name}

    app.include_router(upload.router)
    log.info("app_created", port=cfg.port)
    return app


app = create_app()


def run() -> None:
    cfg = app.state.settings
    uvicorn.run(
        "document_service.main:app",
        host=cfg.host,
        port=cfg.port,
        reload=cfg.environment == "development",
    )


if __name__ == "__main__":
    run()
