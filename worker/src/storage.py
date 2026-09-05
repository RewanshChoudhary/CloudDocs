import cloudinary
import os
from dotenv import load_dotenv

load_dotenv()
CLOUD_API_SECRET=os.getenv("CLOUD_API_SECRET")
CLOUD_NAME=os.getenv("CLOUD_NAME")
CLOUD_API_KEY=os.getenv("CLOUD_API_KEY")
cloudinary.config(
    cloud_name=CLOUD_NAME,
    api_key=CLOUD_API_KEY,
    api_secret=CLOUD_API_SECRET
   
)

def download_raw_video(bucket: str, raw_key: str, destination_path: str) -> None:

    return None


def upload_processed_file(bucket: str, local_path: str, output_key: str, content_type: str) -> None:
    return None


def upload_all_outputs(bucket: str, video_id: str, output_paths: dict[str, str]) -> dict[str, str]:
    return {}
