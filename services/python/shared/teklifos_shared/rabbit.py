import json
import logging
from typing import Any

import pika
from pika.adapters.blocking_connection import BlockingChannel

from teklifos_shared.settings import ServiceSettings

logger = logging.getLogger(__name__)

EXCHANGE = "teklifos.events"


class RabbitPublisher:
    def __init__(self, settings: ServiceSettings) -> None:
        self._url = settings.rabbitmq_url
        self._connection: pika.BlockingConnection | None = None
        self._channel: BlockingChannel | None = None

    def connect(self) -> None:
        if self._connection and self._connection.is_open:
            return
        params = pika.URLParameters(self._url)
        self._connection = pika.BlockingConnection(params)
        self._channel = self._connection.channel()
        self._channel.exchange_declare(exchange=EXCHANGE, exchange_type="topic", durable=True)

    def close(self) -> None:
        if self._connection and self._connection.is_open:
            self._connection.close()
        self._connection = None
        self._channel = None

    def publish(self, routing_key: str, payload: dict[str, Any]) -> None:
        self.connect()
        assert self._channel is not None
        body = json.dumps(payload, ensure_ascii=False).encode("utf-8")
        self._channel.basic_publish(
            exchange=EXCHANGE,
            routing_key=routing_key,
            body=body,
            properties=pika.BasicProperties(
                delivery_mode=2,
                content_type="application/json",
            ),
        )
        logger.info("published", extra={"routing_key": routing_key})
