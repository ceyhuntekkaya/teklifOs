from __future__ import annotations

import io
from typing import BinaryIO

import boto3
from botocore.client import Config

from teklifos_shared.settings import ServiceSettings


class S3Client:
    def __init__(self, settings: ServiceSettings) -> None:
        self._bucket = settings.s3_bucket
        self._client = boto3.client(
            "s3",
            endpoint_url=settings.s3_endpoint_url,
            aws_access_key_id=settings.s3_access_key,
            aws_secret_access_key=settings.s3_secret_key,
            config=Config(signature_version="s3v4"),
            region_name=settings.s3_region,
        )

    def put_object(self, key: str, body: bytes | BinaryIO, content_type: str | None = None) -> str:
        extra = {"ContentType": content_type} if content_type else {}
        if isinstance(body, bytes):
            body = io.BytesIO(body)
        self._client.upload_fileobj(body, self._bucket, key, ExtraArgs=extra)
        return f"s3://{self._bucket}/{key}"

    def get_object(self, key: str) -> bytes:
        buf = io.BytesIO()
        self._client.download_fileobj(self._bucket, key, buf)
        return buf.getvalue()
