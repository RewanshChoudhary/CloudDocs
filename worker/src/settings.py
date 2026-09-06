from dataclasses import dataclass
import os


@dataclass(frozen=True)
class Settings:
    aws_region: str
    aws_endpoint_url: str | None
    bucket_name: str
    queue_url: str
    database_url: str
    raw_prefix: str
    processed_prefix: str
    workspace_dir: str
    wait_time_seconds: int
    visibility_timeout: int


def load_settings() -> Settings:
    return Settings(
        aws_region=os.getenv("AWS_REGION", "us-east-1"),
        aws_endpoint_url=os.getenv("AWS_ENDPOINT_URL") or None,
        bucket_name=os.getenv("MEDIA_S3_BUCKET", "clouddocs-media-local"),
        queue_url=os.getenv("MEDIA_SQS_QUEUE_URL", ""),
        database_url=os.getenv(
            "DATABASE_URL",
            "postgresql://postgres:postgres@localhost:5432/clouddocs",
        ),
        raw_prefix=os.getenv("RAW_PREFIX", "raw"),
        processed_prefix=os.getenv("PROCESSED_PREFIX", "processed"),
        workspace_dir=os.getenv("WORKSPACE_DIR", "/tmp/clouddocs-worker"),
        wait_time_seconds=int(os.getenv("SQS_WAIT_TIME_SECONDS", "20")),
        visibility_timeout=int(os.getenv("SQS_VISIBILITY_TIMEOUT", "900")),
    )
