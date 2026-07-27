from __future__ import annotations

import hashlib
import struct
from dataclasses import dataclass

MAGIC = {
    b"%PDF": "application/pdf",
    b"\x50\x4b\x03\x04": "application/zip",  # xlsx/docx
    b"\x89PNG": "image/png",
    b"\xff\xd8\xff": "image/jpeg",
}

MAX_BYTES = 25 * 1024 * 1024


@dataclass
class ValidationResult:
    ok: bool
    detected_type: str | None
    error: str | None = None


def validate_magic(data: bytes, declared: str | None) -> ValidationResult:
    if len(data) == 0:
        return ValidationResult(False, None, "empty file")
    if len(data) > MAX_BYTES:
        return ValidationResult(False, None, "file too large")
    detected = None
    for magic, mime in MAGIC.items():
        if data.startswith(magic):
            detected = mime
            break
    if detected is None:
        return ValidationResult(False, None, "unknown file type")
    if declared and not _compatible(declared, detected):
        return ValidationResult(False, detected, "content type mismatch")
    return ValidationResult(True, detected, None)


def sha256_hex(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def _compatible(declared: str, detected: str) -> bool:
    if declared == detected:
        return True
    if declared.startswith("application/vnd.openxmlformats") and detected == "application/zip":
        return True
    return False


def zip_bomb_guard(data: bytes) -> None:
    if len(data) < 8:
        return
    # crude check: zip with huge uncompressed size in local header
    if data[:2] == b"PK" and len(data) > 30:
        uncompressed = struct.unpack("<I", data[18:22])[0]
        if uncompressed > 200_000_000:
            raise ValueError("zip bomb suspected")
