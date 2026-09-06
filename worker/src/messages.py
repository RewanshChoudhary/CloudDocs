import json
from urllib.parse import unquote_plus


import boto3


from .settings import Settings


def sqs_client(settings: Settings):
    return boto3.client(
        "sqs",
        region_name=settings.aws_region,
        endpoint_url=settings.aws_endpoint_url,
    )


def receive_messages(settings: Settings) -> list[dict]:
    response = sqs_client(settings).receive_message(
        QueueUrl=settings.queue_url,
        MaxNumberOfMessages=1,
        WaitTimeSeconds=settings.wait_time_seconds,
        VisibilityTimeout=settings.visibility_timeout,
    )
    return response.get("Messages", [])


def parse_message(message_body: str) -> dict:
    payload = json.loads(message_body)
    records = payload.get("Records") or []
    if not records:
        raise ValueError("SQS message does not contain S3 Records")

    record = records[0]
    s3 = record.get("s3") or {}
    bucket = (s3.get("bucket") or {}).get("name")
    raw_key = unquote_plus((s3.get("object") or {}).get("key") or "")
    if not bucket or not raw_key:
        raise ValueError("S3 event is missing bucket or object key")

    parts = raw_key.split("/")
    if len(parts) < 3 or parts[0] != "raw":
        raise ValueError(f"Unexpected raw video key: {raw_key}")

    return {
        "bucket": bucket,
        "raw_key": raw_key,
        "video_id": parts[1],
    }


def delete_message(settings: Settings, receipt_handle: str) -> None:
    sqs_client(settings).delete_message(
        QueueUrl=settings.queue_url,
        ReceiptHandle=receipt_handle,
    )
