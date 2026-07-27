from datetime import datetime
from uuid import UUID, uuid4

from fastapi import APIRouter, HTTPException, status
from pydantic import BaseModel, EmailStr, Field

router = APIRouter(prefix="/api/v1/mailboxes", tags=["mailboxes"])


class MailboxCreate(BaseModel):
    email: EmailStr
    display_name: str | None = None
    imap_host: str = Field(..., min_length=1)
    imap_port: int = Field(default=993, ge=1, le=65535)
    forward_address: EmailStr | None = None


class MailboxResponse(BaseModel):
    id: UUID
    email: EmailStr
    display_name: str | None
    imap_host: str
    imap_port: int
    forward_address: EmailStr | None = None
    verification_status: str = "PENDING"
    last_poll_at: datetime | None = None
    last_error: str | None = None


_mailboxes: dict[UUID, MailboxResponse] = {}


@router.get("", response_model=list[MailboxResponse])
async def list_mailboxes() -> list[MailboxResponse]:
    return list(_mailboxes.values())


@router.post("", response_model=MailboxResponse, status_code=status.HTTP_201_CREATED)
async def create_mailbox(payload: MailboxCreate) -> MailboxResponse:
    mailbox_id = uuid4()
    forward = payload.forward_address or payload.email
    mailbox = MailboxResponse(
        id=mailbox_id,
        email=payload.email,
        display_name=payload.display_name,
        imap_host=payload.imap_host,
        imap_port=payload.imap_port,
        forward_address=forward,
        verification_status="PENDING",
    )
    _mailboxes[mailbox_id] = mailbox
    return mailbox


@router.post("/{mailbox_id}/verify", response_model=MailboxResponse)
async def verify_mailbox(mailbox_id: UUID) -> MailboxResponse:
    mailbox = _mailboxes.get(mailbox_id)
    if mailbox is None:
        raise HTTPException(status_code=404, detail="Mailbox not found")
    updated = mailbox.model_copy(update={"verification_status": "VERIFIED"})
    _mailboxes[mailbox_id] = updated
    return updated


@router.get("/{mailbox_id}", response_model=MailboxResponse)
async def get_mailbox(mailbox_id: UUID) -> MailboxResponse:
    mailbox = _mailboxes.get(mailbox_id)
    if mailbox is None:
        raise HTTPException(status_code=404, detail="Mailbox not found")
    return mailbox


@router.delete("/{mailbox_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_mailbox(mailbox_id: UUID) -> None:
    if mailbox_id not in _mailboxes:
        raise HTTPException(status_code=404, detail="Mailbox not found")
    del _mailboxes[mailbox_id]
