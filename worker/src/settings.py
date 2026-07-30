from dataclasses import dataclass


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
        aws_region="",
        aws_endpoint_url=None,
        bucket_name="",
        queue_url="",
        database_url="",
        raw_prefix="",
        processed_prefix="",
        workspace_dir="",
        wait_time_seconds=0,
        visibility_timeout=0,
    )
