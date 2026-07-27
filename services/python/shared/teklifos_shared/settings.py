from pydantic import Field
from pydantic_settings import BaseSettings, SettingsConfigDict


class ServiceSettings(BaseSettings):
    """Common environment-backed settings for Python services."""

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore",
    )

    service_name: str = Field(default="teklifos-service", validation_alias="SERVICE_NAME")
    host: str = Field(default="0.0.0.0", validation_alias="HOST")
    port: int = Field(default=8000, validation_alias="PORT")
    log_level: str = Field(default="INFO", validation_alias="LOG_LEVEL")
    environment: str = Field(default="development", validation_alias="ENVIRONMENT")

    jwt_issuer: str = Field(default="https://teklifos.local", validation_alias="JWT_ISSUER")
    jwt_audience: str | None = Field(default=None, validation_alias="JWT_AUDIENCE")
    jwt_jwks_url: str | None = Field(default=None, validation_alias="JWT_JWKS_URL")

    rabbitmq_url: str = Field(
        default="amqp://teklifos:teklifos_dev@localhost:5672/",
        validation_alias="RABBITMQ_URL",
    )

    s3_endpoint_url: str = Field(
        default="http://localhost:9000",
        validation_alias="S3_ENDPOINT_URL",
    )
    s3_access_key: str = Field(default="teklifos", validation_alias="S3_ACCESS_KEY")
    s3_secret_key: str = Field(default="teklifos_dev_minio", validation_alias="S3_SECRET_KEY")
    s3_region: str = Field(default="us-east-1", validation_alias="S3_REGION")
    s3_bucket: str = Field(default="teklifos", validation_alias="S3_BUCKET")
