package com.example.CloudDocs.dto.video;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record VideoUploadRequest(
        @NotBlank String originalFilename,
        @NotBlank String contentType,
        @Positive long fileSize
) {
}
