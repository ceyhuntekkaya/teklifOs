#!/usr/bin/env python3
"""Import all service apps to verify dependencies and package layout (CI)."""

from __future__ import annotations

import importlib
import sys

SERVICES = (
    "mail_ingestion_service.main",
    "document_service.main",
    "ocr_service.main",
    "ai_service.main",
    "pdf_render_service.main",
)


def main() -> int:
    for module in SERVICES:
        mod = importlib.import_module(module)
        factory = getattr(mod, "create_app", None)
        if factory is None:
            print(f"FAIL: {module} has no create_app", file=sys.stderr)
            return 1
        app = factory()
        if app is None:
            print(f"FAIL: {module} create_app returned None", file=sys.stderr)
            return 1
        print(f"OK: {module}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
