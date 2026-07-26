package com.example.CloudDocs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class FileUploadResponseDto {
    private Long id;
    private String originalName;
    private String cloudinaryUrl;
    private String cloudinaryPublicId;
    private String fileType;
    private Long fileSize;
    private Integer downloadCount;
    private LocalDateTime uploadedAt;
    private String uploadedBy;
}
