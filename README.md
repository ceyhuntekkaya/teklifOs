# TeklifOS

Çok kiracılı RFQ → eşleştirme → fiyatlandırma → teklif platformu.

## Yapı

- `services/java` — Spring Boot 4.1 mikroservisleri (gateway, identity, master-data, pricing, rfq, quote, notification)
- `services/python` — Belge, OCR, AI, PDF, e-posta servisleri (FastAPI)
- `apps/web` — Next.js 16 frontend (BFF auth cookie)
- `infra/compose` — Docker Compose (Postgres, RabbitMQ, Valkey, MinIO, observability)

## Hızlı başlangıç

```bash
cp .env.example .env
cd infra/compose && docker compose up -d postgres rabbitmq valkey minio
cd ../../services/java && ./gradlew :identity-service:bootRun
cd ../python && uv sync --all-packages
cd ../../apps/web && npm install && npm run dev
```

Demo: `admin@demo.local` / `Demo1234!` (tenant: `demo`)

Detaylı faz takibi: [docs/ROADMAP.md](docs/ROADMAP.md)
