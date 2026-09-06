import logging
import shutil
from pathlib import Path


from .database import get_video_status, mark_completed, mark_failed, mark_processing
from .messages import delete_message, parse_message, receive_messages
from .settings import Settings, load_settings
from .storage import download_raw_video, upload_all_outputs
from .transcoder import transcode_video

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s %(levelname)s %(message)s",
)
logger = logging.getLogger(__name__)


def main() -> None:
    settings = load_settings()
    if not settings.queue_url:
        raise RuntimeError("MEDIA_SQS_QUEUE_URL is required")

    logger.info("worker started")
    while True:
        for message in receive_messages(settings):
            process_message(settings, message)


def process_message(settings: Settings, message: dict) -> None:
    receipt_handle = message["ReceiptHandle"]
    event = parse_message(message["Body"])
    video_id = event["video_id"]
    raw_key = event["raw_key"]
    bucket = event["bucket"]
    workspace = Path(settings.workspace_dir) / video_id
    input_path = workspace / "raw" / Path(raw_key).name
    output_dir = workspace / "processed"

    try:
        status = get_video_status(settings, video_id)
        if status == "COMPLETED":
            logger.info("video_id=%s duplicate completed message skipped", video_id)
            delete_message(settings, receipt_handle)
            return

        mark_processing(settings, video_id)
        logger.info("video_id=%s processing started", video_id)
        download_raw_video(settings, bucket, raw_key, input_path)
        output_paths = transcode_video(input_path, output_dir)
        output_keys = upload_all_outputs(settings, bucket, video_id, output_paths)
        mark_completed(settings, video_id, output_keys)
        delete_message(settings, receipt_handle)
        logger.info("video_id=%s processing completed", video_id)
    except Exception as exc:
        logger.exception("video_id=%s processing failed", video_id)
        mark_failed(settings, video_id, str(exc))
        raise
    finally:
        shutil.rmtree(workspace, ignore_errors=True)


if __name__ == "__main__":
    main()
