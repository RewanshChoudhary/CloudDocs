# Project Context

## Libraries & Frameworks
- **Python 3.12 worker skeleton** - The worker directory is structural only: `settings`, `messages`, `storage`, `transcoder`, `database`, and `worker` modules contain placeholder functions with no AWS, database, or FFmpeg behavior.

## Architecture
- **Async video pipeline scaffold** - Spring Boot auth/media code that existed before this session remains intact. New video pipeline files are placeholders for metadata, presigned upload, S3/SQS, worker, and status-flow responsibilities.
- **`videos` table uses UUID primary keys** - Matches the README blueprint for stable public-facing video identifiers while keeping the existing `users.id` BIGINT relationship intact.

## Conventions
- **Video API namespace** - New media-transcoding placeholder endpoints live under `/api/v1/videos` and currently return `501 Not Implemented`.
- **S3 key layout** - Raw uploads use `raw/{videoId}/{sanitizedOriginalFilename}`. Processed outputs use `processed/{videoId}/{rendition}.mp4` plus `processed/{videoId}/thumbnail.jpg`.

## Gotchas & Workarounds
- **Local AWS services need bootstrapping** - `docker-compose.yml` includes LocalStack, but the S3 bucket, SQS queue, and S3 event notification still need a bootstrap script or infrastructure definition before end-to-end local processing will work.

## Tooling
- **Docker Compose** - Runs Postgres, LocalStack, and the Python worker scaffold for local development.
