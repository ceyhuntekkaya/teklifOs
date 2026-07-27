from __future__ import annotations

import json
import logging
import threading

import pika
from teklifos_shared.settings import ServiceSettings

from document_service.worker import QUEUE_DOCUMENT_PROCESS, DocumentProcessor

logger = logging.getLogger(__name__)


def start_consumer(settings: ServiceSettings) -> threading.Thread:
    processor = DocumentProcessor(settings)

    def _run() -> None:
        params = pika.URLParameters(settings.rabbitmq_url)
        connection = pika.BlockingConnection(params)
        channel = connection.channel()
        channel.queue_declare(queue=QUEUE_DOCUMENT_PROCESS, durable=True)
        channel.basic_qos(prefetch_count=1)

        def on_message(ch, method, properties, body: bytes) -> None:
            try:
                payload = json.loads(body.decode("utf-8"))
                processor.handle_message(payload)
                ch.basic_ack(delivery_tag=method.delivery_tag)
            except Exception:
                logger.exception("document_process_failed")
                ch.basic_nack(delivery_tag=method.delivery_tag, requeue=False)

        channel.basic_consume(queue=QUEUE_DOCUMENT_PROCESS, on_message_callback=on_message)
        logger.info("document_consumer_started", extra={"queue": QUEUE_DOCUMENT_PROCESS})
        channel.start_consuming()

    thread = threading.Thread(target=_run, name="document-consumer", daemon=True)
    thread.start()
    return thread
