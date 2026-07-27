# TeklifOS operasyon runbook (özet)

## Postgres erişilemiyor
1. `docker compose ps postgres`
2. Log: `docker compose logs postgres --tail=100`
3. Disk dolu mu kontrol et; gerekirse volume yedekten geri yükle

## RabbitMQ DLQ birikimi
1. Management UI :15672
2. DLQ kuyruğunu incele, `processed_message` ile idempotent consumer doğrula
3. Mesajı düzeltip yeniden yayınla veya manuel işaretle

## LLM sağlayıcı kesintisi
- `ai-service` circuit breaker açıkken RFQ hattı deterministik eşleştirme ile devam eder
- Satırlar `UNMATCHED` olarak insan incelemesine düşer

## Yedekleme
- Postgres: pgBackRest PITR (production)
- MinIO: bucket replikasyonu
- Üç ayda bir geri yükleme tatbikatı zorunlu
