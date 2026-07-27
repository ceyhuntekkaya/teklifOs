from fastapi import APIRouter
from pydantic import BaseModel, Field

router = APIRouter(prefix="/pdf", tags=["pdf"])


class QuoteLineItem(BaseModel):
    description: str
    quantity: float = Field(default=1.0, gt=0)
    unit_price: float = Field(default=0.0, ge=0)


class RenderQuoteRequest(BaseModel):
    quote_id: str = Field(..., min_length=1)
    customer_name: str = Field(..., min_length=1)
    currency: str = Field(default="TRY", min_length=3, max_length=3)
    line_items: list[QuoteLineItem] = Field(default_factory=list)


class RenderQuoteResponse(BaseModel):
    quote_id: str
    pdf_url: str
    page_count: int
    bytes_length: int


@router.post("/render-quote", response_model=RenderQuoteResponse)
async def render_quote(payload: RenderQuoteRequest) -> RenderQuoteResponse:
    stub_bytes = 1024 + len(payload.line_items) * 128
    return RenderQuoteResponse(
        quote_id=payload.quote_id,
        pdf_url=f"s3://teklifos/quotes/{payload.quote_id}.pdf",
        page_count=max(1, (len(payload.line_items) + 9) // 10),
        bytes_length=stub_bytes,
    )
