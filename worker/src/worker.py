from .database import mark_completed, mark_failed, mark_processing
from .messages import delete_message, parse_message, receive_messages
from .settings import load_settings
from .storage import download_raw_video, upload_all_outputs
from .transcoder import transcode_video


def main() -> None:
    return None


def process_message(message: dict) -> None:
    return None


if __name__ == "__main__":
    main()
