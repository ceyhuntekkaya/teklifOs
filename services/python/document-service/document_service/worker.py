from __future__ import annotations

import logging
import socket
import uuid
from typing import Any

from teklifos_shared.rabbit import RabbitPublisher
from teklifos_shared.s3 import S3Client
from teklifos_shared.settings import ServiceSettings

from document_service.pipeline.extractors import extract_pdf, extract_xlsx, preview_json
from document_service.pipeline.file_type import validate_magic, zip_bomb_guard
from document_service.pipeline.line_items import infer_line_items

logger = logging.getLogger(__name__)

RFQ_DOCUMENT_RECEIVED = "rfq.document.received"
RFQ_DOCUMENT_SCANNED = "rfq.document.scanned"
RFQ_DOCUMENT_PARSED = "rfq.document.parsed"
RFQ_DOCUMENT_OCR_REQUESTED = "rfq.document.ocr.requested"
RFQ_DOCUMENT_EXTRACTED = "rfq.document.extracted"
QUEUE_DOCUMENT_PROCESS = "rfq.document.process"


def scan_clamav(data: bytes, host: str = "localhost", port: int = 3310) -> None:
    try:
        with socket.create_connection((host, port), timeout=2.0) as sock:
            sock.sendall(b"zINSTREAM\0")
            size = len(data)
            sock.sendall(size.to_bytes(4, "big") + data)
            sock.sendall(b"\0")
            response = sock.recv(4096)
            if b"FOUND" in response:
                raise ValueError("malware detected")
    except (OSError, TimeoutError):
        logger.warning("clamav_unavailable_skip_scan")


class DocumentProcessor:
    def __init__(self, settings: ServiceSettings) -> None:
        self._settings = settings
        self._s3 = S3Client(settings)
        self._rabbit = RabbitPublisher(settings)

    def handle_message(self, payload: dict[str, Any]) -> None:
        message_id = payload.get("messageId") or str(uuid.uuid4())
        tenant_id = payload["tenantId"]
        document_id = payload["documentId"]
        rfq_id = payload["rfqId"]
        storage_key = payload["storageKey"]
        content_type = payload.get("contentType", "application/octet-stream")

        data = self._s3.get_object(storage_key)
        zip_bomb_guard(data)
        validation = validate_magic(data, content_type)
        if not validation.ok:
            self._fail(message_id, payload, validation.error or "invalid file")
            return

        scan_clamav(data)
        self._publish_stage(
            message_id,
            tenant_id,
            rfq_id,
            document_id,
            RFQ_DOCUMENT_SCANNED,
            {"stage": "SCANNED"},
        )

        extraction = self._extract(data, validation.detected_type or content_type)
        self._publish_stage(
            message_id,
            tenant_id,
            rfq_id,
            document_id,
            RFQ_DOCUMENT_PARSED,
            {"stage": "PARSED", "pageCount": extraction.page_count},
        )

        if extraction.needs_ocr:
            ocr_payload = {
                "messageId": message_id,
                "tenantId": tenant_id,
                "rfqId": rfq_id,
                "documentId": document_id,
                "storageKey": storage_key,
                "stage": "OCR",
            }
            self._rabbit.publish(RFQ_DOCUMENT_OCR_REQUESTED, ocr_payload)
            return

        preview = preview_json(extraction.text, extraction.tables)
        preview["lineItems"] = infer_line_items(extraction.text, extraction.tables)
        self._publish_stage(
            message_id,
            tenant_id,
            rfq_id,
            document_id,
            RFQ_DOCUMENT_EXTRACTED,
            {
                "stage": "EXTRACTED",
                "extractedPreview": preview,
                "pageCount": extraction.page_count,
            },
        )

    def _extract(self, data: bytes, content_type: str):
        if content_type == "application/pdf" or data.startswith(b"%PDF"):
            return extract_pdf(data)
        if "spreadsheet" in content_type or data.startswith(b"PK"):
            return extract_xlsx(data)
        return extract_pdf(data) if data.startswith(b"%PDF") else extract_xlsx(data)

    def _publish_stage(
        self,
        message_id: str,
        tenant_id: str,
        rfq_id: str,
        document_id: str,
        routing_key: str,
        extra: dict[str, Any],
    ) -> None:
        body = {
            "messageId": message_id,
            "tenantId": tenant_id,
            "rfqId": rfq_id,
            "documentId": document_id,
            **extra,
        }
        self._rabbit.publish(routing_key, body)

    def _fail(self, message_id: str, payload: dict[str, Any], error: str) -> None:
        body = {
            "messageId": message_id,
            "tenantId": payload["tenantId"],
            "rfqId": payload["rfqId"],
            "documentId": payload["documentId"],
            "stage": "FAILED",
            "error": error,
        }
        self._rabbit.publish(RFQ_DOCUMENT_EXTRACTED, body)
