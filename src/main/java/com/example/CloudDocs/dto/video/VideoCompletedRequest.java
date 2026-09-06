package com.example.CloudDocs.dto.video;

public record VideoCompletedRequest(
        String s3Key720p,
        String s3Key480p,
        String s3Key360p,
        String s3ThumbnailKey
) {
}
