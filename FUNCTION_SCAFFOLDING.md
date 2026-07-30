# CloudDocs Function Scaffolding

This document describes how the functions should be structured for the asynchronous video and media transcoding pipeline described in `README.md`.

## 1. Spring Boot API

The Spring Boot backend should stay focused on fast HTTP work: authentication, metadata persistence, presigned URL generation, and read-only status/playback queries.

### Auth Functions

Location:

```text
src/main/java/com/example/CloudDocs/service/AuthService.java
src/main/java/com/example/CloudDocs/controller/AuthController.java
```

Functions:

```text
registerUser(request)
  - Validate username, email, password.
  - Reject duplicate email/username.
  - Hash password.
  - Save user.
  - Return JWT response.

loginUser(request)
  - Authenticate credentials.
  - Generate access token.
  - Return JWT response.

getAuthenticatedUser(authentication)
  - Read current principal from Spring Security.
  - Load user from database.
  - Throw if missing.
```

## 2. Video Upload API

Purpose: create a database row and return a short-lived S3 upload URL. The actual video file should not pass through Spring Boot.

Suggested location:

```text
controller/video/VideoController.java
service/video/VideoService.java
repository/video/VideoRepository.java
model/video/Video.java
model/video/VideoStatus.java
```

Controller functions:

```text
createPresignedUpload(request, authentication)
  - Authenticate user.
  - Call videoService.createPresignedUpload().
  - Return HTTP 202 with videoId, uploadUrl, raw S3 key, expiry once implemented.

listVideos(authentication)
  - Authenticate user.
  - Return all videos owned by user once implemented.

getVideo(videoId, authentication)
  - Authenticate user.
  - Return one video status and playback URLs if completed once implemented.
```

Service functions:

```text
createPresignedUpload(user, request)
  - Generate videoId.
  - Build raw S3 key: raw/{videoId}/{filename}.
  - Insert video row with status PENDING.
  - Generate S3 PUT presigned URL.
  - Return upload response.

listVideos(user)
  - Fetch videos by user ordered by createdAt desc.
  - Map each entity to response shape.

getVideo(user, videoId)
  - Fetch video.
  - Enforce owner/admin access.
  - Generate download URLs only for completed outputs.
  - Return response shape.

presignDownload(s3Key)
  - Generate short-lived S3 GET URL.

sanitizeFilename(filename)
  - Replace unsafe characters before using filename in S3 key.
```

Repository functions:

```text
findByUserOrderByCreatedAtDesc(user)
findById(videoId)
save(video)
```

## 3. Video State Model

Entity fields:

```text
Video
  id: UUID
  user: User
  originalFilename: String
  contentType: String
  fileSize: Long
  s3RawKey: String
  status: VideoStatus
  errorMessage: String?
  s3720pKey: String?
  s3480pKey: String?
  s3360pKey: String?
  s3ThumbnailKey: String?
  createdAt: LocalDateTime
  updatedAt: LocalDateTime
```

State enum:

```text
PENDING
PROCESSING
COMPLETED
FAILED
```

Valid transitions:

```text
PENDING -> PROCESSING
PROCESSING -> COMPLETED
PROCESSING -> FAILED
FAILED -> PROCESSING    # optional retry path
```

## 4. AWS Adapter Functions

Purpose: isolate AWS-specific logic from business services.

Suggested location:

```text
config/aws/AwsMediaConfig.java
config/aws/AwsMediaProperties.java
service/aws/S3MediaStorageService.java
service/aws/SqsMediaQueueService.java
```

S3 functions:

```text
createUploadUrl(bucket, key, contentType, ttl)
  - Build PutObjectRequest.
  - Generate presigned PUT URL.

createDownloadUrl(bucket, key, ttl)
  - Build GetObjectRequest.
  - Generate presigned GET URL.

buildRawKey(videoId, filename)
  - Return raw/{videoId}/{safeFilename}.

buildProcessedKey(videoId, rendition)
  - Return processed/{videoId}/{rendition}.mp4.

buildThumbnailKey(videoId)
  - Return processed/{videoId}/thumbnail.jpg.
```

SQS functions:

```text
sendVideoUploadedMessage(videoId, bucket, rawKey)
  - Optional if Spring publishes queue messages directly.
  - Not required if S3 event notifications publish to SQS.

parseS3EventMessage(body)
  - Extract bucket and object key from S3 event JSON.
```

## 5. Python Worker

The worker should own long-running CPU-heavy processing.

Suggested location:

```text
worker/src/settings.py
worker/src/worker.py
worker/src/transcoder.py
worker/src/storage.py
worker/src/database.py
worker/src/messages.py
```

Main worker loop:

```text
main()
  - Load settings.
  - Create S3, SQS, and database clients.
  - Poll SQS forever.
  - Process each message.
  - Delete message only after successful completion.
```

Message functions:

```text
receive_messages(queueUrl)
  - Long-poll SQS.
  - Return one or more messages.

parse_message(messageBody)
  - Support native S3 event messages.
  - Extract bucket and raw S3 key.
  - Derive videoId from raw/{videoId}/{filename}.

delete_message(receiptHandle)
  - Remove completed message from SQS.
```

Database functions:

```text
mark_processing(videoId)
  - Set status PROCESSING.
  - Clear previous error if retrying.

mark_completed(videoId, outputKeys)
  - Set status COMPLETED.
  - Save 720p, 480p, 360p, thumbnail keys.
  - Clear error message.

mark_failed(videoId, errorMessage)
  - Set status FAILED.
  - Save compact failure reason.
```

Storage functions:

```text
download_raw_video(bucket, rawKey, destinationPath)
  - Download raw object from S3 to worker temp directory.

upload_processed_file(bucket, localPath, outputKey, contentType)
  - Upload one output file to S3.

upload_all_outputs(bucket, videoId, outputPaths)
  - Upload all renditions and thumbnail.
  - Return output key map.
```

Transcoding functions:

```text
transcode_video(inputPath, outputDir)
  - Generate 720p, 480p, and 360p MP4 files.
  - Generate thumbnail.
  - Return local output paths.

run_ffmpeg(command)
  - Execute FFmpeg command.
  - Raise if FFmpeg exits non-zero.

create_rendition(inputPath, outputPath, scale, bitrate)
  - Create one MP4 rendition.

create_thumbnail(inputPath, outputPath)
  - Capture one frame as JPEG.
```

Processing orchestration:

```text
process_message(message)
  - Parse bucket/rawKey/videoId.
  - Mark PROCESSING.
  - Download raw video.
  - Transcode outputs.
  - Upload processed outputs.
  - Mark COMPLETED.
  - Delete SQS message.
  - On error: mark FAILED and leave/delete message based on retry policy.
```

## 6. Database Schema Functions

Migration responsibilities:

```text
create_users_table()
  - Already present in current project.

create_videos_table()
  - Store upload metadata, S3 keys, status, timestamps.

create_video_status_indexes()
  - Index user_id + created_at.
  - Index status for worker/admin queries.
```

## 7. Local Development Utilities

Suggested scripts:

```text
scripts/bootstrap-localstack.sh
  - Create local S3 bucket.
  - Create SQS queue and DLQ.
  - Configure S3 event notification to SQS.

scripts/run-api.sh
  - Start Spring Boot with local environment variables.

scripts/run-worker.sh
  - Start Python worker locally.

scripts/upload-test-video.sh
  - Request presigned URL.
  - PUT a sample video to S3.
  - Poll video status.
```

## 8. End-to-End Flow

```text
1. registerUser/loginUser
2. createPresignedUpload
3. Client uploads file directly to S3
4. S3 publishes event to SQS
5. worker.receive_messages
6. worker.process_message
7. transcoder.transcode_video
8. storage.upload_all_outputs
9. database.mark_completed
10. getVideo returns status and playback URLs
```
