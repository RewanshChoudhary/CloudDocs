# CloudDocs

CloudDocs is a Spring Boot document/media backend with an MVP asynchronous video transcoding pipeline.

The video pipeline is intentionally lean: authenticated clients request a presigned S3 upload URL, upload raw video directly to S3, S3 sends an event to SQS, and a Python worker pulls the message, transcodes the video with FFmpeg, uploads processed outputs, and updates PostgreSQL status.

## MVP Architecture

```text
Client
  | REST/JWT
  v
Spring Boot API
  | creates videos row + presigned PUT URL
  v
Private S3 bucket
  | ObjectCreated event for raw/
  v
SQS queue + DLQ
  | long polling
  v
Python worker + FFmpeg
  | status/output updates
  v
PostgreSQL
```

## What Is In Scope

- Spring Boot API with JWT auth and `/api/v1/videos`.
- PostgreSQL metadata table for video status and output keys.
- S3 presigned upload and download URLs.
- S3 object-created notifications to SQS.
- Python worker using boto3, psycopg, and FFmpeg.
- Simple state flow: `PENDING -> PROCESSING -> COMPLETED` or `FAILED`.
- SQS DLQ for messages that repeatedly fail.
- Local development with Docker Compose, Postgres, LocalStack, API, and worker.

## What Is Deferred

These are useful later but intentionally outside the MVP:

- AWS Step Functions
- EventBridge
- SNS or webhook notifications
- Virus/content scanning
- Redis, Celery, distributed locks
- Lambda, ECS autoscaling, CloudFront
- Prometheus, Grafana, X-Ray

The MVP should prove the core backend patterns first: direct-to-S3 uploads, queue-based async processing, deterministic output keys, idempotent duplicate handling, and durable status tracking.

## Video API

### Create Presigned Upload URL

```http
POST /api/v1/videos/presigned
Authorization: Bearer <jwt>
Content-Type: application/json

{
  "originalFilename": "sample.mp4",
  "contentType": "video/mp4",
  "fileSize": 1048576
}
```

Response:

```json
{
  "success": true,
  "message": "Presigned upload URL created",
  "data": {
    "videoId": "00000000-0000-0000-0000-000000000000",
    "uploadUrl": "http://...",
    "rawKey": "raw/{videoId}/sample.mp4",
    "expiresAt": "2026-09-06T00:00:00Z"
  }
}
```

### List Videos

```http
GET /api/v1/videos
Authorization: Bearer <jwt>
```

### Get Video

```http
GET /api/v1/videos/{id}
Authorization: Bearer <jwt>
```

When processing is complete, the response includes short-lived download URLs for available renditions and the thumbnail.

## S3 Key Layout

- Raw upload: `raw/{videoId}/{sanitizedOriginalFilename}`
- Processed 720p: `processed/{videoId}/720p.mp4`
- Processed 480p: `processed/{videoId}/480p.mp4`
- Processed 360p: `processed/{videoId}/360p.mp4`
- Thumbnail: `processed/{videoId}/thumbnail.jpg`

## Local Development

Start the local stack:

```bash
docker compose up --build
```

The compose stack starts:

- `postgres` on port `5432`
- `localstack` on port `4566`
- `api` on port `8080`
- `worker`

LocalStack bootstrap creates:

- S3 bucket: `clouddocs-media-local`
- SQS queue: `clouddocs-media-events`
- SQS DLQ: `clouddocs-media-events-dlq`
- S3 notification from `raw/` object creation to SQS

## Environment Variables

Spring Boot:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `JWT_SECRET`
- `AWS_REGION`
- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`
- `AWS_S3_ENDPOINT`
- `MEDIA_S3_BUCKET`

Python worker:

- `AWS_REGION`
- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`
- `AWS_ENDPOINT_URL`
- `MEDIA_S3_BUCKET`
- `MEDIA_SQS_QUEUE_URL`
- `DATABASE_URL`
- `WORKSPACE_DIR`

## Production Notes

For AWS deployment, keep the same app shape and swap LocalStack values for real AWS resources. Prefer an EC2 instance role over checked-in AWS keys. Configure S3 server-side encryption and lifecycle rules for raw uploads once the MVP works locally.
