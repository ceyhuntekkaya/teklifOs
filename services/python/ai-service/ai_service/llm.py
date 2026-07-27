from typing import Any, Protocol, runtime_checkable


@runtime_checkable
class LlmGateway(Protocol):
    """Abstraction over LLM providers."""

    async def complete(self, prompt: str, *, model: str | None = None) -> str: ...

    async def extract_structured(
        self,
        prompt: str,
        schema: dict[str, Any],
        *,
        model: str | None = None,
    ) -> dict[str, Any]: ...


class OpenAiAdapter:
    """Stub OpenAI adapter; wire to the official SDK when OPENAI_API_KEY is set."""

    def __init__(self, api_key: str | None = None, default_model: str = "gpt-4o-mini") -> None:
        self._api_key = api_key
        self._default_model = default_model

    async def complete(self, prompt: str, *, model: str | None = None) -> str:
        _ = (self._api_key, model or self._default_model)
        return f"[stub OpenAI completion] {prompt[:200]}"

    async def extract_structured(
        self,
        prompt: str,
        schema: dict[str, Any],
        *,
        model: str | None = None,
    ) -> dict[str, Any]:
        _ = (self._api_key, model or self._default_model, prompt, schema)
        return {"stub": True, "fields": list(schema.get("properties", {}).keys())}
