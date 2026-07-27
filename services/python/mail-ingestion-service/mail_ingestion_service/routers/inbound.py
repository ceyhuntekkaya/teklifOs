import uuid
from typing import Any

from fastapi import APIRouter, Request
from pydantic import BaseModel, EmailStr
from teklifos_shared.rabbit import RabbitPublisher
from teklifos_shared.settings import ServiceSettings

router = APIRouter(prefix="/api/v1/inbound", tags=["inbound"])


class InboundWebhookPayload(BaseModel):
    tenant_id: str
    from_email: EmailStr
    subject: str | None = None
    body_text: str | None = None
    attachment_keys: list[str] = []


@router.post("/webhook")
async def inbound_webhook(request: Request, payload: InboundWebhookPayload) -> dict[str, Any]:
    settings: ServiceSettings = request.app.state.settings
    publisher = RabbitPublisher(settings)
    message_id = str(uuid.uuid4())
    event = {
        "messageId": message_id,
        "tenantId": payload.tenant_id,
        "rfqId": str(uuid.uuid4()),
        "documentId": str(uuid.uuid4()),
        "source": "EMAIL",
        "fromEmail": str(payload.from_email),
        "subject": payload.subject,
        "bodyText": payload.body_text,
        "attachmentKeys": payload.attachment_keys,
    }
    publisher.publish("rfq.document.received", event)
    return {"accepted": True, "messageId": message_id}
