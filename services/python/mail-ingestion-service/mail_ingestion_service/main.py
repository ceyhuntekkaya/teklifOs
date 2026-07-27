import uvicorn
from fastapi import FastAPI
from teklifos_shared.logging import configure_logging, get_logger
from teklifos_shared.middleware import CorrelationIdMiddleware
from teklifos_shared.settings import ServiceSettings

from mail_ingestion_service.routers import inbound, mailboxes

DEFAULT_PORT = 9001


def create_app(settings: ServiceSettings | None = None) -> FastAPI:
    cfg = settings or ServiceSettings(service_name="mail-ingestion-service", port=DEFAULT_PORT)
    configure_logging(service_name=cfg.service_name, log_level=cfg.log_level)
    log = get_logger(__name__)

    app = FastAPI(title="Mail Ingestion Service", version="0.1.0")
    app.add_middleware(CorrelationIdMiddleware)
    app.state.settings = cfg

    @app.get("/health")
    async def health() -> dict[str, str]:
        return {"status": "ok", "service": cfg.service_name}

    app.include_router(mailboxes.router)
    app.include_router(inbound.router)
    log.info("app_created", port=cfg.port)
    return app


app = create_app()


def run() -> None:
    cfg = app.state.settings
    uvicorn.run(
        "mail_ingestion_service.main:app",
        host=cfg.host,
        port=cfg.port,
        reload=cfg.environment == "development",
    )


if __name__ == "__main__":
    run()
