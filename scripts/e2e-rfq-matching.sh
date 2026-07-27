#!/usr/bin/env bash
# Uçtan uca: login → RFQ upload (Excel) → pipeline → eşleştirme satırları
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
GATEWAY="${GATEWAY_URL:-http://localhost:8080}"
IDENTITY="${IDENTITY_URL:-http://localhost:8081}"
RFQ="${RFQ_URL:-http://localhost:8084}"
MASTER="${MASTER_DATA_URL:-http://localhost:8082}"
INTERNAL_KEY="${TEKLIFOS_INTERNAL_API_KEY:-dev-internal-key}"
DEMO_TENANT="a1b2c3d4-e5f6-4789-a012-3456789abcde"

log() { printf '==> %s\n' "$*"; }

wait_http() {
  local url=$1 name=$2
  for _ in $(seq 1 60); do
    if curl -sf -o /dev/null "$url" 2>/dev/null; then
      log "$name hazır"
      return 0
    fi
    sleep 2
  done
  echo "Timeout: $name ($url)" >&2
  return 1
}

wait_http "${IDENTITY}/actuator/health" "identity-service"
wait_http "${MASTER}/actuator/health" "master-data-service"
wait_http "${RFQ}/actuator/health" "rfq-service"

log "Giriş (demo tenant)"
LOGIN_JSON=$(curl -sf -X POST "${IDENTITY}/api/v1/auth/login" \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@demo.local","password":"Demo1234!","tenantSlug":"demo"}')
TOKEN=$(echo "$LOGIN_JSON" | python3 -c "import sys,json; print(json.load(sys.stdin)['accessToken'])")

TMP_XLSX=$(mktemp /tmp/teklifos-rfq-XXXXXX.xlsx)
(cd "$ROOT/services/python" && uv run python - <<PY
import openpyxl
wb = openpyxl.Workbook()
ws = wb.active
ws.append(["Kod", "Açıklama", "Miktar"])
ws.append(["3RV2011-1GA10", "Siemens motor koruma", "2"])
ws.append(["POMPA-25", "Hidrolik pompa", "1"])
wb.save("$TMP_XLSX")
PY
)

log "RFQ yükleme"
UPLOAD=$(curl -sf -X POST "${RFQ}/api/v1/rfqs/upload" \
  -H "Authorization: Bearer ${TOKEN}" \
  -F "files=@${TMP_XLSX};type=application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
RFQ_ID=$(echo "$UPLOAD" | python3 -c "import sys,json; print(json.load(sys.stdin)['rfqId'])")
rm -f "$TMP_XLSX"
log "RFQ id=$RFQ_ID"

log "Pipeline + eşleştirme bekleniyor (max 90s)"
LINES=0
for _ in $(seq 1 45); do
  DETAIL=$(curl -sf -H "Authorization: Bearer ${TOKEN}" "${RFQ}/api/v1/rfqs/${RFQ_ID}")
  LINES=$(echo "$DETAIL" | python3 -c "import sys,json; d=json.load(sys.stdin); print(len(d.get('lines') or []))")
  STATUS=$(echo "$DETAIL" | python3 -c "import sys,json; print(json.load(sys.stdin).get('status',''))")
  if [[ "$LINES" -ge 1 ]] && [[ "$STATUS" == "READY_FOR_REVIEW" || "$STATUS" == "MATCHED" ]]; then
    echo "$DETAIL" | python3 -m json.tool
    log "Tamam: $LINES satır, status=$STATUS"
    break
  fi
  sleep 2
done

if [[ "$LINES" -lt 1 ]]; then
  echo "RFQ satırları oluşmadı. document-service çalışıyor mu? Son detay:" >&2
  curl -sf -H "Authorization: Bearer ${TOKEN}" "${RFQ}/api/v1/rfqs/${RFQ_ID}" | python3 -m json.tool >&2 || true
  exit 1
fi

log "Master-data matching harness"
export MASTER_DATA_URL="$MASTER"
export TEKLIFOS_INTERNAL_API_KEY="$INTERNAL_KEY"
if command -v uv >/dev/null 2>&1; then
  (cd "$ROOT/services/python" && uv run python scripts/matching_quality_harness.py) || true
else
  python3 "$ROOT/services/python/scripts/matching_quality_harness.py" || true
fi

log "Bitti. Web: http://localhost:3000/rfqs/${RFQ_ID}"
