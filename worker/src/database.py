import psycopg


from .settings import Settings


def get_video_status(settings: Settings, video_id: str) -> str | None:
    with psycopg.connect(settings.database_url) as conn:
        with conn.cursor() as cur:
            cur.execute("SELECT status FROM videos WHERE id = %s", (video_id,))
            row = cur.fetchone()
            return row[0] if row else None


def mark_processing(settings: Settings, video_id: str) -> None:
    with psycopg.connect(settings.database_url) as conn:
        with conn.cursor() as cur:
            cur.execute(
                """
                UPDATE videos
                SET status = 'PROCESSING', error_message = NULL, updated_at = CURRENT_TIMESTAMP
                WHERE id = %s AND status <> 'COMPLETED'
                """,
                (video_id,),
            )
            if cur.rowcount == 0:
                raise LookupError(f"Video {video_id} was not found or is already completed")


def mark_completed(settings: Settings, video_id: str, output_keys: dict[str, str]) -> None:
    with psycopg.connect(settings.database_url) as conn:
        with conn.cursor() as cur:
            cur.execute(
                """
                UPDATE videos
                SET status = 'COMPLETED',
                    error_message = NULL,
                    s3_720p_key = %s,
                    s3_480p_key = %s,
                    s3_360p_key = %s,
                    s3_thumbnail_key = %s,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = %s
                """,
                (
                    output_keys.get("720p"),
                    output_keys.get("480p"),
                    output_keys.get("360p"),
                    output_keys.get("thumbnail"),
                    video_id,
                ),
            )
            if cur.rowcount == 0:
                raise LookupError(f"Video {video_id} was not found")


def mark_failed(settings: Settings, video_id: str, error_message: str) -> None:
    with psycopg.connect(settings.database_url) as conn:
        with conn.cursor() as cur:
            cur.execute(
                """
                UPDATE videos
                SET status = 'FAILED',
                    error_message = %s,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = %s AND status <> 'COMPLETED'
                """,
                (error_message[:1000], video_id),
            )
