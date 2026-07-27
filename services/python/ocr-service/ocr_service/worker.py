from __future__ import annotations

import json
import logging
import threading
import uuid
from typing import Any

import pika
from teklifos_shared.rabbit import RabbitPublisher
from teklifos_shared.s3 import S3Client
from teklifos_shared.settings import ServiceSettings

logger = logging.getLogger(__name__)

QUEUE_OCR = "rfq.document.ocr"
RFQ_DOCUMENT_OCR_COMPLETED = "rfq.document.ocr.completed"
RFQ_DOCUMENT_EXTRACTED = "rfq.document.extracted"


def _ocr_text(data: bytes) -> str:
    try:
        import io as iolib

        import pytesseract
        from PIL import Image

        img = Image.open(iolib.BytesIO(data))
        return pytesseract.image_to_string(img, lang="tur+eng")
    except Exception:
        return ""


class OcrProcessor:
    def __init__(self, settings: ServiceSettings) -> None:
        self._s3 = S3Client(settings)
        self._rabbit = RabbitPublisher(settings)

    def handle(self, payload: dict[str, Any]) -> None:
        message_id = payload.get("messageId") or str(uuid.uuid4())
        storage_key = payload["storageKey"]
        data = self._s3.get_object(storage_key)
        text = _ocr_text(data)
        if len(text.strip()) < 5:
            text = "[OCR stub: no text extracted]"

        completed = {
            "messageId": message_id,
            "tenantId": payload["tenantId"],
            "rfqId": payload["rfqId"],
            "documentId": payload["documentId"],
            "stage": "OCR",
        }
        self._rabbit.publish(RFQ_DOCUMENT_OCR_COMPLETED, completed)

        extracted = {
            "messageId": message_id,
            "tenantId": payload["tenantId"],
            "rfqId": payload["rfqId"],
            "documentId": payload["documentId"],
            "stage": "EXTRACTED",
            "extractedPreview": {"textPreview": text[:4000], "tableCount": 0},
        }
        self._rabbit.publish(RFQ_DOCUMENT_EXTRACTED, extracted)


def start_ocr_consumer(settings: ServiceSettings) -> threading.Thread:
    processor = OcrProcessor(settings)

    def _run() -> None:
        connection = pika.BlockingConnection(pika.URLParameters(settings.rabbitmq_url))
        channel = connection.channel()
        channel.queue_declare(queue=QUEUE_OCR, durable=True)

        def on_message(ch, method, _props, body: bytes) -> None:
            try:
                processor.handle(json.loads(body.decode("utf-8")))
                ch.basic_ack(method.delivery_tag)
            except Exception:
                logger.exception("ocr_failed")
                ch.basic_nack(method.delivery_tag, requeue=False)

        channel.basic_consume(QUEUE_OCR, on_message)
        channel.start_consuming()

    t = threading.Thread(target=_run, daemon=True, name="ocr-consumer")
    t.start()
    return t
