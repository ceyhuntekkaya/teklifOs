#!/usr/bin/env bash
# Yerel geliştirme: altyapı + Java/Python servisleri (gateway hariç, doğrudan portlar).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
COMPOSE_DIR="$ROOT/infra/compose"
JAVA_DIR="$ROOT/services/java"
PY_DIR="$ROOT/services/python"
PID_DIR="$ROOT/.dev-pids"
mkdir -p "$PID_DIR"

export DB_HOST=localhost
export RABBITMQ_HOST=localhost
export RABBITMQ_USER=teklifos
export RABBITMQ_PASSWORD=teklifos_dev
export REDIS_HOST=localhost
export MINIO_ENDPOINT=http://localhost:9000
export MINIO_ROOT_USER=teklifos
export MINIO_ROOT_PASSWORD=teklifos_dev_minio
export TEKLIFOS_INTERNAL_API_KEY=dev-internal-key
export JWT_JWKS_URI=http://localhost:8081/.well-known/jwks.json

log() { printf '[dev-stack] %s\n' "$*"; }

start_java() {
  local module=$1
  local pidfile="$PID_DIR/${module}.pid"
  if [[ -f "$pidfile" ]] && kill -0 "$(cat "$pidfile")" 2>/dev/null; then
    log "$module zaten çalışıyor (pid $(cat "$pidfile"))"
    return 0
  fi
  log "Başlatılıyor $module"
  (
    cd "$JAVA_DIR"
    nohup ./gradlew ":${module}:bootRun" --no-daemon \
      >"$PID_DIR/${module}.log" 2>&1 &
    echo $! >"$pidfile"
  )
}

start_python() {
  local pkg=$1 port=$2
  local pidfile="$PID_DIR/${pkg}.pid"
  if [[ -f "$pidfile" ]] && kill -0 "$(cat "$pidfile")" 2>/dev/null; then
    log "$pkg zaten çalışıyor"
    return 0
  fi
  log "Başlatılıyor $pkg :$port"
  (
    cd "$PY_DIR"
    export PATH="${HOME}/.local/bin:${PATH}"
    nohup uv run "$pkg" >"$PID_DIR/${pkg}.log" 2>&1 &
    echo $! >"$pidfile"
  )
}

case "${1:-up}" in
  up)
    log "Docker altyapı (postgres, rabbitmq, valkey, minio)"
    docker compose -f "$COMPOSE_DIR/docker-compose.yml" up -d postgres rabbitmq valkey minio 2>&1 | tail -5 || {
      log "Uyarı: postgres 5432 dolu olabilir — yerel Postgres kullanılıyorsa DB'lerin oluşturulduğundan emin olun (infra/compose/postgres/init)."
    }
    start_java identity-service
    start_java master-data-service
    start_java rfq-service
    start_python ai-service
    start_python document-service
    log "Loglar: $PID_DIR/*.log — E2E: $ROOT/scripts/e2e-rfq-matching.sh"
    ;;
  down)
    for f in "$PID_DIR"/*.pid; do
      [[ -f "$f" ]] || continue
      kill "$(cat "$f")" 2>/dev/null || true
      rm -f "$f"
    done
    docker compose -f "$COMPOSE_DIR/docker-compose.yml" stop postgres rabbitmq valkey minio 2>/dev/null || true
    log "Durduruldu"
    ;;
  *)
    echo "Kullanım: $0 up|down" >&2
    exit 1
    ;;
esac
