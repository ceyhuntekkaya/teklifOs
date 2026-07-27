from __future__ import annotations

from typing import Any
from uuid import UUID

from fastapi import APIRouter
from pydantic import BaseModel, ConfigDict, Field

from ai_service.cost import cosine_similarity, embed_text, ledger, llm_breaker

router = APIRouter(prefix="/ai", tags=["ai"])


class EmbedRequest(BaseModel):
    text: str = Field(..., min_length=1)


class EmbedResponse(BaseModel):
    dimensions: int
    vector: list[float]
    model: str


@router.post("/embed", response_model=EmbedResponse)
async def embed(payload: EmbedRequest) -> EmbedResponse:
    vector = embed_text(payload.text)
    ledger.record(prompt_tokens=len(payload.text) // 4, completion_tokens=0, model="embed-stub")
    return EmbedResponse(dimensions=len(vector), vector=vector, model="hash-embed-v1")


class RerankCandidateIn(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    product_id: UUID = Field(alias="productId")
    sku: str
    score: float
    source: str


class RerankRequest(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    raw_text: str | None = Field(default=None, alias="rawText")
    customer_code: str | None = Field(default=None, alias="customerCode")
    candidates: list[RerankCandidateIn] = Field(default_factory=list)


class RerankCandidateOut(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    product_id: UUID = Field(serialization_alias="productId")
    sku: str
    score: float
    source: str


class RerankResponse(BaseModel):
    candidates: list[RerankCandidateOut]


@router.post("/rerank", response_model=RerankResponse)
async def rerank(payload: RerankRequest) -> RerankResponse:
    if not llm_breaker.allow():
        return RerankResponse(
            candidates=[
                RerankCandidateOut(
                    product_id=c.product_id,
                    sku=c.sku,
                    score=c.score,
                    source=c.source,
                )
                for c in payload.candidates
            ]
        )
    try:
        query = " ".join(
            p for p in (payload.raw_text, payload.customer_code) if p and p.strip()
        )
        q_vec = embed_text(query or " ")
        ranked: list[RerankCandidateOut] = []
        for c in payload.candidates:
            doc = f"{c.sku} {c.source}"
            sim = cosine_similarity(q_vec, embed_text(doc))
            blended = min(0.99, c.score * 0.6 + max(0.0, sim) * 0.4)
            ranked.append(
                RerankCandidateOut(
                    product_id=c.product_id,
                    sku=c.sku,
                    score=blended,
                    source=c.source + "+RERANK",
                )
            )
        ranked.sort(key=lambda x: x.score, reverse=True)
        ledger.record(
            prompt_tokens=len(query) // 4,
            completion_tokens=len(payload.candidates) * 8,
            model="gpt-4o-mini-stub",
        )
        llm_breaker.on_success()
        return RerankResponse(candidates=ranked)
    except Exception:
        llm_breaker.on_failure()
        raise


@router.get("/usage")
async def usage() -> dict[str, Any]:
    return {
        "total_calls": ledger.total_calls,
        "prompt_tokens": ledger.prompt_tokens,
        "completion_tokens": ledger.completion_tokens,
        "estimated_usd": round(ledger.estimated_usd, 6),
        "circuit_open": not llm_breaker.allow(),
    }
