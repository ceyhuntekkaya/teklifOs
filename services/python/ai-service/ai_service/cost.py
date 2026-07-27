"""Deterministic dev embeddings and token cost accounting."""

from __future__ import annotations

import hashlib
import math
import threading
import time
from dataclasses import dataclass, field


def embed_text(text: str, dimensions: int = 384) -> list[float]:
    digest = hashlib.sha256(text.encode("utf-8")).digest()
    out: list[float] = []
    i = 0
    while len(out) < dimensions:
        chunk = digest if i == 0 else hashlib.sha256(digest + i.to_bytes(2, "big")).digest()
        for b in chunk:
            out.append((b / 127.5) - 1.0)
            if len(out) >= dimensions:
                break
        i += 1
    norm = math.sqrt(sum(v * v for v in out)) or 1.0
    return [v / norm for v in out]


def cosine_similarity(a: list[float], b: list[float]) -> float:
    return sum(x * y for x, y in zip(a, b, strict=False))


@dataclass
class CostLedger:
    total_calls: int = 0
    prompt_tokens: int = 0
    completion_tokens: int = 0
    estimated_usd: float = 0.0
    _lock: threading.Lock = field(default_factory=threading.Lock, repr=False)

    def record(self, *, prompt_tokens: int, completion_tokens: int, model: str) -> None:
        rate_in = 0.15 / 1_000_000 if "mini" in model else 2.5 / 1_000_000
        rate_out = 0.6 / 1_000_000 if "mini" in model else 10.0 / 1_000_000
        cost = prompt_tokens * rate_in + completion_tokens * rate_out
        with self._lock:
            self.total_calls += 1
            self.prompt_tokens += prompt_tokens
            self.completion_tokens += completion_tokens
            self.estimated_usd += cost


class CircuitBreaker:
    def __init__(self, failure_threshold: int = 3, open_seconds: float = 60.0) -> None:
        self.failure_threshold = failure_threshold
        self.open_seconds = open_seconds
        self._failures = 0
        self._opened_at: float | None = None
        self._lock = threading.Lock()

    def allow(self) -> bool:
        with self._lock:
            if self._opened_at is None:
                return True
            if time.monotonic() - self._opened_at >= self.open_seconds:
                self._opened_at = None
                self._failures = 0
                return True
            return False

    def on_success(self) -> None:
        with self._lock:
            self._failures = 0
            self._opened_at = None

    def on_failure(self) -> None:
        with self._lock:
            self._failures += 1
            if self._failures >= self.failure_threshold:
                self._opened_at = time.monotonic()


ledger = CostLedger()
llm_breaker = CircuitBreaker()
