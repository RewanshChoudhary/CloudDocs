from pathlib import Path
import subprocess


def transcode_video(input_path: Path, output_dir: Path) -> dict[str, Path]:
    output_dir.mkdir(parents=True, exist_ok=True)
    outputs = {
        "720p": output_dir / "720p.mp4",
        "480p": output_dir / "480p.mp4",
        "360p": output_dir / "360p.mp4",
        "thumbnail": output_dir / "thumbnail.jpg",
    }

    create_rendition(input_path, outputs["720p"], "-2:720", "2800k")
    create_rendition(input_path, outputs["480p"], "-2:480", "1400k")
    create_rendition(input_path, outputs["360p"], "-2:360", "800k")
    create_thumbnail(input_path, outputs["thumbnail"])
    return outputs


def run_ffmpeg(command: list[str]) -> None:
    subprocess.run(command, check=True, capture_output=True, text=True)


def create_rendition(input_path: Path, output_path: Path, scale: str, bitrate: str) -> None:
    run_ffmpeg([
        "ffmpeg",
        "-y",
        "-i",
        str(input_path),
        "-vf",
        f"scale={scale}",
        "-c:v",
        "libx264",
        "-preset",
        "veryfast",
        "-b:v",
        bitrate,
        "-c:a",
        "aac",
        "-b:a",
        "128k",
        "-movflags",
        "+faststart",
        str(output_path),
    ])


def create_thumbnail(input_path: Path, output_path: Path) -> None:
    run_ffmpeg([
        "ffmpeg",
        "-y",
        "-i",
        str(input_path),
        "-frames:v",
        "1",
        "-q:v",
        "2",
        str(output_path),
    ])
