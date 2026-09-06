# CloudDocs Media Worker

Python worker for the CloudDocs asynchronous video pipeline.

The worker long-polls SQS for S3 `ObjectCreated` events under `raw/`, downloads the uploaded video, runs FFmpeg, uploads deterministic processed outputs, and updates the `videos` row in PostgreSQL.

## Runtime Flow

1. Receive one SQS message.
2. Parse the S3 event and extract `video_id`, bucket, and raw key.
3. Skip and delete the message if the video is already `COMPLETED`.
4. Mark the video `PROCESSING`.
5. Download the raw object from S3.
6. Generate `720p`, `480p`, `360p`, and `thumbnail` outputs.
7. Upload outputs to `processed/{videoId}/`.
8. Mark the video `COMPLETED` and delete the SQS message.
9. On failure, mark the video `FAILED` and let SQS retry/redrive to the DLQ.

## Dependencies

- `boto3` for S3/SQS
- `psycopg` for PostgreSQL
- `ffmpeg` installed in the worker Docker image

## Environment

- `AWS_REGION`
- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`
- `AWS_ENDPOINT_URL`
- `MEDIA_S3_BUCKET`
- `MEDIA_SQS_QUEUE_URL`
- `DATABASE_URL`
- `WORKSPACE_DIR`
- `SQS_WAIT_TIME_SECONDS`
- `SQS_VISIBILITY_TIMEOUT`
