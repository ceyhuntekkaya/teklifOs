from uuid import uuid4

from fastapi import APIRouter, File, HTTPException, UploadFile, status
from pydantic import BaseModel, Field

router = APIRouter(prefix="/documents", tags=["documents"])

ALLOWED_CONTENT_TYPES = {
    "application/pdf",
    "image/png",
    "image/jpeg",
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
}
MAX_UPLOAD_BYTES = 25 * 1024 * 1024


class UploadValidateResponse(BaseModel):
    document_id: str
    filename: str
    content_type: str
    size_bytes: int
    valid: bool
    message: str = Field(default="ok")


@router.post("/upload/validate", response_model=UploadValidateResponse)
async def upload_validate(file: UploadFile = File(...)) -> UploadValidateResponse:
    if file.content_type not in ALLOWED_CONTENT_TYPES:
        raise HTTPException(
            status_code=status.HTTP_415_UNSUPPORTED_MEDIA_TYPE,
            detail=f"Unsupported content type: {file.content_type}",
        )

    data = await file.read()
    size = len(data)
    if size == 0:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Empty file")
    if size > MAX_UPLOAD_BYTES:
        raise HTTPException(
            status_code=status.HTTP_413_REQUEST_ENTITY_TOO_LARGE,
            detail=f"File exceeds {MAX_UPLOAD_BYTES} bytes",
        )

    return UploadValidateResponse(
        document_id=str(uuid4()),
        filename=file.filename or "unknown",
        content_type=file.content_type or "application/octet-stream",
        size_bytes=size,
        valid=True,
        message="stub validation passed",
    )
