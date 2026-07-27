from fastapi import APIRouter
from pydantic import BaseModel, Field

router = APIRouter(prefix="/ocr", tags=["ocr"])


class ProcessPageRequest(BaseModel):
    document_id: str = Field(..., min_length=1)
    page_number: int = Field(..., ge=1)
    language: str = Field(default="tur+eng")


class ProcessPageResponse(BaseModel):
    document_id: str
    page_number: int
    text: str
    confidence: float


@router.post("/process-page", response_model=ProcessPageResponse)
async def process_page(payload: ProcessPageRequest) -> ProcessPageResponse:
    stub_text = (
        f"[stub OCR] document={payload.document_id} page={payload.page_number} "
        f"lang={payload.language}"
    )
    return ProcessPageResponse(
        document_id=payload.document_id,
        page_number=payload.page_number,
        text=stub_text,
        confidence=0.0,
    )
