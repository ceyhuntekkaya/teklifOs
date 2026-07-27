from __future__ import annotations

import io
from dataclasses import dataclass
from typing import Any

import fitz  # pymupdf
import openpyxl


@dataclass
class ExtractionResult:
    text: str
    tables: list[list[list[str]]]
    page_count: int
    needs_ocr: bool


def extract_pdf(data: bytes) -> ExtractionResult:
    doc = fitz.open(stream=data, filetype="pdf")
    texts: list[str] = []
    for page in doc:
        texts.append(page.get_text())
    full = "\n".join(texts).strip()
    needs_ocr = len(full) < 40
    return ExtractionResult(full, [], doc.page_count, needs_ocr)


def extract_xlsx(data: bytes) -> ExtractionResult:
    wb = openpyxl.load_workbook(io.BytesIO(data), read_only=True, data_only=True)
    tables: list[list[list[str]]] = []
    lines: list[str] = []
    for sheet in wb.worksheets:
        rows: list[list[str]] = []
        for row in sheet.iter_rows(max_row=5000, values_only=True):
            cells = ["" if c is None else str(c) for c in row]
            if any(cells):
                rows.append(cells)
                lines.append(" | ".join(cells))
        if rows:
            tables.append(rows)
    wb.close()
    return ExtractionResult("\n".join(lines), tables, 1, False)


def preview_json(text: str, tables: list[list[list[str]]]) -> dict[str, Any]:
    return {"textPreview": text[:4000], "tableCount": len(tables)}
