from pathlib import Path


import boto3


from .settings import Settings


def s3_client(settings: Settings):
    return boto3.client(
        "s3",
        region_name=settings.aws_region,
        endpoint_url=settings.aws_endpoint_url,
    )


def download_raw_video(settings: Settings, bucket: str, raw_key: str, destination_path: Path) -> None:
    destination_path.parent.mkdir(parents=True, exist_ok=True)
    s3_client(settings).download_file(bucket, raw_key, str(destination_path))


def upload_processed_file(
    settings: Settings,
    bucket: str,
    local_path: Path,
    output_key: str,
    content_type: str,
) -> None:
    s3_client(settings).upload_file(
        str(local_path),
        bucket,
        output_key,
        ExtraArgs={"ContentType": content_type},
    )


def upload_all_outputs(
    settings: Settings,
    bucket: str,
    video_id: str,
    output_paths: dict[str, Path],
) -> dict[str, str]:
    uploaded_keys = {}
    for rendition, local_path in output_paths.items():
        if rendition == "thumbnail":
            output_key = f"{settings.processed_prefix}/{video_id}/thumbnail.jpg"
            content_type = "image/jpeg"
        else:
            output_key = f"{settings.processed_prefix}/{video_id}/{rendition}.mp4"
            content_type = "video/mp4"

        upload_processed_file(settings, bucket, local_path, output_key, content_type)
        uploaded_keys[rendition] = output_key

    return uploaded_keys
