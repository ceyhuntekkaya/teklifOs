# TeklifOS Geliştirme Yol Haritası

| Faz | Konu | Durum |
|-----|------|--------|
| 0 | Monorepo, Docker, Gateway, CI, Web iskelet | Tamamlandı |
| 1 | Kimlik, tenant, JWT, auth UI | Devam ediyor |
| 2 | Ana veri + içe aktarma | İskelet |
| 3 | Fiyatlandırma motoru | İskelet |
| 4 | Belge / e-posta hattı | Tamamlandı (RFQ upload, saga, document/ocr/mail servisleri, UI) |
| 5 | AI + eşleştirme | Katmanlı motor (exact/alias/trigram), ai-service embed+rerank, RFQ MATCHED hattı, harness |
| 6 | RFQ inceleme UI | Planlandı |
| 7 | Teklif + onay + PDF | İskelet |
| 8 | Gönderim + takip | İskelet |
| 9 | Raporlama | Planlandı |
| 10 | Production sertleştirme | Planlandı |

## Çalıştırma

```bash
# Altyapı
cd infra/compose && docker compose up -d postgres rabbitmq valkey minio

# Java servisleri (örnek)
cd services/java && ./gradlew :identity-service:bootRun

# Python (örnek)
cd services/python && uv sync && uv run mail-ingestion-service

# Web
pnpm install && pnpm dev:web
```

Demo giriş: `admin@demo.local` / `Demo1234!` (tenant slug: `demo`)
