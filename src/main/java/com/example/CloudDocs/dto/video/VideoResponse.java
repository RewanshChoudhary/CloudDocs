package com.example.CloudDocs.dto.video;

import com.example.CloudDocs.model.video.VideoStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record VideoResponse(
        UUID id,
        String originalFilename,
        String contentType,
        long fileSize,
        VideoStatus status,
        String errorMessage,
        String rawKey,
        Map<String, String> downloadUrls,
        String thumbnailUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
