#!/usr/bin/env python3
"""Offline matching quality harness against master-data match API."""

from __future__ import annotations

import argparse
import json
import os
import sys
import urllib.error
import urllib.request

DEMO_TENANT = "a1b2c3d4-e5f6-4789-a012-3456789abcde"
DEFAULT_BASE = os.environ.get("MASTER_DATA_URL", "http://localhost:8082")
INTERNAL_KEY = os.environ.get("TEKLIFOS_INTERNAL_API_KEY", "dev-internal-key")

CASES = [
    {
        "name": "exact_sku",
        "body": {"rawText": "Siemens motor koruma", "customerCode": "3RV2011-1GA10"},
        "expect_status": "AUTO",
    },
    {
        "name": "customer_alias",
        "body": {
            "rawText": "pompa",
            "customerCode": "POMPA-25",
            "customerId": "c1000001-0000-4000-8000-000000000001",
        },
        "expect_status": "AUTO",
    },
    {
        "name": "fuzzy_name",
        "body": {"rawText": "Siemens motor koruma rolesi 4.5-6.3A", "customerCode": None},
        "expect_status": None,
    },
]


def post_match(base: str, body: dict) -> dict:
    payload = json.dumps(body).encode("utf-8")
    req = urllib.request.Request(
        f"{base.rstrip('/')}/api/v1/matching/match",
        data=payload,
        headers={
            "Content-Type": "application/json",
            "X-TeklifOS-Internal-Key": INTERNAL_KEY,
            "X-TeklifOS-Tenant-Id": DEMO_TENANT,
        },
        method="POST",
    )
    with urllib.request.urlopen(req, timeout=15) as resp:
        return json.loads(resp.read().decode("utf-8"))


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--base-url", default=DEFAULT_BASE)
    args = parser.parse_args()
    passed = 0
    for case in CASES:
        try:
            result = post_match(args.base_url, case["body"])
        except urllib.error.URLError as exc:
            print(f"FAIL {case['name']}: {exc}", file=sys.stderr)
            continue
        status = result.get("status")
        candidates = result.get("candidates") or []
        ok = case["expect_status"] is None or status == case["expect_status"]
        if ok and candidates:
            ok = True
        elif case["expect_status"] and status != case["expect_status"]:
            ok = False
        mark = "OK" if ok else "FAIL"
        print(f"{mark} {case['name']}: status={status} candidates={len(candidates)}")
        if ok:
            passed += 1
    print(f"Passed {passed}/{len(CASES)}")
    return 0 if passed == len(CASES) else 1


if __name__ == "__main__":
    raise SystemExit(main())
