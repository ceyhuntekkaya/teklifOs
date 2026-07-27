from __future__ import annotations

import re
from typing import Any


def infer_line_items(text: str, tables: list[list[list[str]]]) -> list[dict[str, Any]]:
  """Heuristic line extraction from spreadsheet tables or pipe-separated text."""
  items: list[dict[str, Any]] = []
  line_no = 1
  for table in tables:
    if not table:
      continue
    header = [c.lower() for c in table[0]]
    sku_idx = _col(header, ("sku", "kod", "ürün", "urun", "malzeme", "part"))
    qty_idx = _col(header, ("miktar", "adet", "qty", "quantity"))
    desc_idx = _col(header, ("açıklama", "aciklama", "description", "tanım", "tanim"))
    start = 1 if sku_idx >= 0 or qty_idx >= 0 else 0
    for row in table[start:]:
      if not any(cell.strip() for cell in row):
        continue
      customer_sku = row[sku_idx].strip() if sku_idx >= 0 and sku_idx < len(row) else ""
      desc = row[desc_idx].strip() if desc_idx >= 0 and desc_idx < len(row) else ""
      qty_raw = row[qty_idx].strip() if qty_idx >= 0 and qty_idx < len(row) else ""
      if not customer_sku and not desc:
        customer_sku = row[0].strip() if row else ""
      if not customer_sku and not desc:
        continue
      items.append(
        {
          "lineNumber": line_no,
          "rawCustomerSku": customer_sku or None,
          "rawDescription": desc or customer_sku,
          "quantity": _parse_qty(qty_raw),
          "unitCode": "EA",
        }
      )
      line_no += 1
  if not items and text:
    for raw in text.splitlines():
      if "|" not in raw:
        continue
      parts = [p.strip() for p in raw.split("|")]
      if len(parts) < 2:
        continue
      items.append(
        {
          "lineNumber": line_no,
          "rawCustomerSku": parts[0] or None,
          "rawDescription": parts[1] if len(parts) > 1 else parts[0],
          "quantity": _parse_qty(parts[2]) if len(parts) > 2 else None,
          "unitCode": "EA",
        }
      )
      line_no += 1
  return items


def _col(header: list[str], names: tuple[str, ...]) -> int:
  for i, cell in enumerate(header):
    for n in names:
      if n in cell:
        return i
  return -1


def _parse_qty(value: str | None) -> float | None:
  if not value:
    return None
  cleaned = re.sub(r"[^\d,.\-]", "", value).replace(",", ".")
  try:
    return float(cleaned)
  except ValueError:
    return None
