## RFQ upload → eşleştirme (E2E)

### Önkoşullar

- Docker: RabbitMQ + MinIO (Postgres için `5432` boş değilse yerel Postgres + `infra/compose/postgres/init` scriptleri)
- Java 21, `uv`, Python workspace

### Tek komutla stack

```bash
./scripts/dev-stack.sh up
# ~2–3 dk sonra:
./scripts/e2e-rfq-matching.sh
```

Loglar: `.dev-pids/*.log`  
Durdurma: `./scripts/dev-stack.sh down`

### Manuel

```bash
cd infra/compose && docker compose up -d rabbitmq valkey minio
# Postgres: compose veya yerel

export DB_HOST=localhost RABBITMQ_HOST=localhost \
  RABBITMQ_USER=teklifos RABBITMQ_PASSWORD=teklifos_dev \
  MINIO_ENDPOINT=http://localhost:9000 \
  TEKLIFOS_INTERNAL_API_KEY=dev-internal-key

cd services/java
./gradlew :identity-service:bootRun   # 8081
./gradlew :master-data-service:bootRun # 8082
./gradlew :rfq-service:bootRun        # 8084

cd services/python && uv sync --all-packages
uv run document-service   # 9002 + Rabbit consumer
uv run ai-service         # 9004 (rerank, opsiyonel)
```

Demo giriş: `admin@demo.local` / `Demo1234!` / tenant `demo`

Web: `npm run dev` → http://localhost:3000/rfqs

### Sorun giderme

| Belirti | Kontrol |
|--------|---------|
| Satır oluşmuyor | `document-service` log, Rabbit kuyruk `rfq.document.process` |
| Hepsi UNMATCHED | `masterdata_db` Flyway V2 seed; tenant `a1b2c3d4-e5f6-4789-a012-3456789abcde` |
| Postgres port çakışması | `docker compose` postgres yerine yerel DB + init SQL |
