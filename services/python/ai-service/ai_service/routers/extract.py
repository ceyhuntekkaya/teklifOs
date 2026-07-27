from typing import Any

from fastapi import APIRouter, Depends
from pydantic import BaseModel, Field

from ai_service.llm import LlmGateway, OpenAiAdapter

router = APIRouter(prefix="/ai", tags=["ai"])


class ExtractRequest(BaseModel):
    text: str = Field(..., min_length=1)
    schema_name: str = Field(default="rfq_line_item")
    hints: dict[str, Any] = Field(default_factory=dict)


class ExtractResponse(BaseModel):
    schema_name: str
    data: dict[str, Any]
    model: str


def get_llm_gateway() -> LlmGateway:
    return OpenAiAdapter()


@router.post("/extract", response_model=ExtractResponse)
async def extract(
    payload: ExtractRequest,
    gateway: LlmGateway = Depends(get_llm_gateway),
) -> ExtractResponse:
    schema = {
        "type": "object",
        "properties": {
            "line_items": {"type": "array"},
            "currency": {"type": "string"},
            "notes": {"type": "string"},
        },
    }
    prompt = f"Extract structured RFQ data from:\n\n{payload.text}"
    if payload.hints:
        prompt += f"\n\nHints: {payload.hints}"

    data = await gateway.extract_structured(prompt, schema)
    return ExtractResponse(schema_name=payload.schema_name, data=data, model="gpt-4o-mini-stub")
