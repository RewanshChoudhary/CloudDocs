package com.example.CloudDocs.dto.video;

import java.time.Instant;
import java.util.UUID;

public record VideoUploadResponse(
        UUID videoId,
        String uploadUrl,
        String rawKey,
        Instant expiresAt
) {
}
