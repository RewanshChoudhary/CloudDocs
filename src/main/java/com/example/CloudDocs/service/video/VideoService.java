package com.example.CloudDocs.service.video;

import com.example.CloudDocs.dto.video.VideoCompletedRequest;
import com.example.CloudDocs.dto.video.VideoFailedRequest;
import com.example.CloudDocs.dto.video.VideoResponse;
import com.example.CloudDocs.dto.video.VideoUploadRequest;
import com.example.CloudDocs.dto.video.VideoUploadResponse;
import com.example.CloudDocs.model.User;
import com.example.CloudDocs.model.video.Video;
import com.example.CloudDocs.model.video.VideoStatus;
import com.example.CloudDocs.repository.video.VideoRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class VideoService {

    private static final Pattern SAFE_FILENAME_CHARS = Pattern.compile("[^A-Za-z0-9._-]");
    private static final Duration UPLOAD_URL_TTL = Duration.ofMinutes(15);
    private static final Duration DOWNLOAD_URL_TTL = Duration.ofMinutes(15);

    private final VideoRepository videoRepository;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Transactional
    public VideoUploadResponse createPresignedUpload(User user, VideoUploadRequest request) {
        if (!request.contentType().startsWith("video/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "contentType must be a video MIME type");
        }

        UUID videoId = UUID.randomUUID();
        String rawKey = buildRawKey(videoId, request.originalFilename());
        Video video = Video.builder()
                .id(videoId)
                .user(user)
                .originalFilename(request.originalFilename())
                .contentType(request.contentType())
                .fileSize(request.fileSize())
                .s3RawKey(rawKey)
                .status(VideoStatus.PENDING)
                .build();

        videoRepository.save(video);
        Instant expiresAt = Instant.now().plus(UPLOAD_URL_TTL);

        return new VideoUploadResponse(
                videoId,
                createUploadUrl(bucketName, rawKey, request.contentType()),
                rawKey,
                expiresAt
        );
    }

    @Transactional(readOnly = true)
    public List<VideoResponse> listVideos(User user) {
        return videoRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public VideoResponse getVideo(User user, UUID videoId) {
        return toResponse(findOwnedVideo(user, videoId));
    }

    public String createUploadUrl(String bucket, String key, String contentType) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(UPLOAD_URL_TTL)
                .putObjectRequest(putObjectRequest)
                .build();
        return s3Presigner.presignPutObject(presignRequest).url().toString();
    }

    public String createDownloadUrl(String bucket, String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(DOWNLOAD_URL_TTL)
                .getObjectRequest(getObjectRequest)
                .build();
        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    public String buildRawKey(UUID videoId, String filename) {
        return "raw/" + videoId + "/" + sanitizeFilename(filename);
    }

    public String buildProcessedKey(UUID videoId, String rendition) {
        return "processed/" + videoId + "/" + rendition + ".mp4";
    }

    public String buildThumbnailKey(UUID videoId) {
        return "processed/" + videoId + "/thumbnail.jpg";
    }

    @Transactional
    public void markProcessing(User user, UUID videoId) {
        Video video = findOwnedVideo(user, videoId);
        if (video.getStatus() == VideoStatus.COMPLETED) {
            return;
        }
        video.setStatus(VideoStatus.PROCESSING);
        video.setErrorMessage(null);
        videoRepository.save(video);
    }

    @Transactional
    public void markCompleted(User user, UUID videoId, VideoCompletedRequest request) {
        Video video = findOwnedVideo(user, videoId);
        video.setStatus(VideoStatus.COMPLETED);
        video.setErrorMessage(null);
        video.setS3Key720p(valueOrDefault(request.s3Key720p(), buildProcessedKey(videoId, "720p")));
        video.setS3Key480p(valueOrDefault(request.s3Key480p(), buildProcessedKey(videoId, "480p")));
        video.setS3Key360p(valueOrDefault(request.s3Key360p(), buildProcessedKey(videoId, "360p")));
        video.setS3ThumbnailKey(valueOrDefault(request.s3ThumbnailKey(), buildThumbnailKey(videoId)));
        videoRepository.save(video);
    }

    @Transactional
    public void markFailed(User user, UUID videoId, VideoFailedRequest request) {
        Video video = findOwnedVideo(user, videoId);
        if (video.getStatus() == VideoStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Completed videos cannot be marked failed");
        }
        video.setStatus(VideoStatus.FAILED);
        video.setErrorMessage(valueOrDefault(request.errorMessage(), "Video processing failed"));
        videoRepository.save(video);
    }

    private Video findOwnedVideo(User user, UUID videoId) {
        return videoRepository.findByIdAndUser(videoId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Video not found"));
    }

    private VideoResponse toResponse(Video video) {
        Map<String, String> downloadUrls = null;
        String thumbnailUrl = null;

        if (video.getStatus() == VideoStatus.COMPLETED) {
            downloadUrls = new LinkedHashMap<>();
            putDownloadUrl(downloadUrls, "720p", video.getS3Key720p());
            putDownloadUrl(downloadUrls, "480p", video.getS3Key480p());
            putDownloadUrl(downloadUrls, "360p", video.getS3Key360p());
            if (video.getS3ThumbnailKey() != null) {
                thumbnailUrl = createDownloadUrl(bucketName, video.getS3ThumbnailKey());
            }
        }

        return new VideoResponse(
                video.getId(),
                video.getOriginalFilename(),
                video.getContentType(),
                video.getFileSize(),
                video.getStatus(),
                video.getErrorMessage(),
                video.getS3RawKey(),
                downloadUrls,
                thumbnailUrl,
                video.getCreatedAt(),
                video.getUpdatedAt()
        );
    }

    private void putDownloadUrl(Map<String, String> downloadUrls, String rendition, String key) {
        if (key != null) {
            downloadUrls.put(rendition, createDownloadUrl(bucketName, key));
        }
    }

    private String sanitizeFilename(String filename) {
        String basename = filename == null ? "" : filename.replace("\\", "/");
        int slashIndex = basename.lastIndexOf("/");
        if (slashIndex >= 0) {
            basename = basename.substring(slashIndex + 1);
        }
        basename = SAFE_FILENAME_CHARS.matcher(basename.trim().replaceAll("\\s+", "_")).replaceAll("_");
        if (basename.isBlank() || ".".equals(basename) || "..".equals(basename)) {
            return "video";
        }
        return basename.length() > 120 ? basename.substring(0, 120) : basename;
    }

    private String valueOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
