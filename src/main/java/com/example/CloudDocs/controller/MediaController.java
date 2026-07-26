package com.example.CloudDocs.controller;

import com.example.CloudDocs.dto.ApiResponse;
import com.example.CloudDocs.dto.FileUploadResponseDto;
import com.example.CloudDocs.exception.ResourceNotFoundException;
import com.example.CloudDocs.model.Comment;
import com.example.CloudDocs.model.FileMetadata;
import com.example.CloudDocs.model.SharedLink;
import com.example.CloudDocs.model.User;
import com.example.CloudDocs.repository.CommentRepository;
import com.example.CloudDocs.repository.FileMetadataRepository;
import com.example.CloudDocs.repository.SharedLinkRepository;
import com.example.CloudDocs.repository.UserRepository;
import com.example.CloudDocs.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/media")
public class MediaController {

    private final CloudinaryService cloudinaryService;
    private final FileMetadataRepository fileMetadataRepository;
    private final UserRepository userRepository;
    private final SharedLinkRepository sharedLinkRepository;
    private final CommentRepository commentRepository;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<FileUploadResponseDto>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            Authentication authentication) throws IOException {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("File is empty"));
        }

        User user = getAuthenticatedUser(authentication);
        Map<String, Object> details = cloudinaryService.uploadFile(file, description);

        FileMetadata metadata = FileMetadata.builder()
                .originalName(file.getOriginalFilename() != null ? file.getOriginalFilename() : "unnamed_file")
                .cloudinaryUrl(details.get("url").toString())
                .cloudinaryPublicId(details.get("publicId").toString())
                .contentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                .fileSize(file.getSize())
                .downloadCount(0)
                .uploadedBy(user)
                .build();

        FileMetadata saved = fileMetadataRepository.save(metadata);
        return ResponseEntity.ok(ApiResponse.success("File uploaded successfully", mapToFileDto(saved)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FileUploadResponseDto>>> getUserFiles(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        List<FileMetadata> files = fileMetadataRepository.findByUploadedBy(user);
        List<FileUploadResponseDto> dtos = files.stream().map(this::mapToFileDto).toList();
        return ResponseEntity.ok(ApiResponse.success("Files retrieved successfully", dtos));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<FileUploadResponseDto>>> searchFiles(@RequestParam("name") String name) {
        List<FileMetadata> files = fileMetadataRepository.findByOriginalNameContainingIgnoreCase(name);
        List<FileUploadResponseDto> dtos = files.stream().map(this::mapToFileDto).toList();
        return ResponseEntity.ok(ApiResponse.success("Files search completed", dtos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FileUploadResponseDto>> getFile(@PathVariable("id") Long id) {
        FileMetadata file = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with id: " + id));

        file.setDownloadCount(file.getDownloadCount() + 1);
        FileMetadata updated = fileMetadataRepository.save(file);

        return ResponseEntity.ok(ApiResponse.success("File retrieved successfully", mapToFileDto(updated)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFile(@PathVariable("id") Long id, Authentication authentication) throws IOException {
        User user = getAuthenticatedUser(authentication);
        FileMetadata file = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with id: " + id));

        if (!file.getUploadedBy().getId().equals(user.getId()) && !"ROLE_ADMIN".equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("You are not authorized to delete this file"));
        }

        cloudinaryService.deleteFile(file.getCloudinaryPublicId());
        fileMetadataRepository.delete(file);

        return ResponseEntity.ok(ApiResponse.success("File deleted successfully", null));
    }

    @PostMapping("/{id}/share")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createShareLink(
            @PathVariable("id") Long id,
            @RequestParam(value = "expiresInHours", defaultValue = "24") int expiresInHours,
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);
        FileMetadata file = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with id: " + id));

        SharedLink link = SharedLink.builder()
                .token(UUID.randomUUID().toString())
                .file(file)
                .sharedBy(user)
                .expiresAt(LocalDateTime.now().plusHours(expiresInHours))
                .isActive(true)
                .build();

        SharedLink savedLink = sharedLinkRepository.save(link);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("shareToken", savedLink.getToken());
        responseData.put("expiresAt", savedLink.getExpiresAt());
        responseData.put("file", mapToFileDto(file));

        return ResponseEntity.ok(ApiResponse.success("Share link created successfully", responseData));
    }

    @GetMapping("/public/share/{token}")
    public ResponseEntity<ApiResponse<FileUploadResponseDto>> getSharedFile(@PathVariable("token") String token) {
        SharedLink link = sharedLinkRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Shared link invalid or expired"));

        if (!Boolean.TRUE.equals(link.getIsActive()) || link.getExpiresAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.GONE).body(ApiResponse.error("Shared link has expired"));
        }

        FileMetadata file = link.getFile();
        file.setDownloadCount(file.getDownloadCount() + 1);
        fileMetadataRepository.save(file);

        return ResponseEntity.ok(ApiResponse.success("Shared file retrieved successfully", mapToFileDto(file)));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<Map<String, Object>>> addComment(
            @PathVariable("id") Long id,
            @RequestBody Map<String, String> body,
            Authentication authentication) {

        String content = body.get("content");
        if (content == null || content.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Comment content cannot be empty"));
        }

        User user = getAuthenticatedUser(authentication);
        FileMetadata file = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with id: " + id));

        Comment comment = Comment.builder()
                .content(content.trim())
                .file(file)
                .author(user)
                .build();

        Comment saved = commentRepository.save(comment);

        Map<String, Object> responseData = Map.of(
                "id", saved.getId(),
                "content", saved.getContent(),
                "author", user.getEmail(),
                "createdAt", saved.getCreatedAt() != null ? saved.getCreatedAt() : LocalDateTime.now()
        );

        return ResponseEntity.ok(ApiResponse.success("Comment added successfully", responseData));
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getComments(@PathVariable("id") Long id) {
        FileMetadata file = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with id: " + id));

        List<Comment> comments = commentRepository.findByFile(file);
        List<Map<String, Object>> responseData = comments.stream().map(c -> Map.<String, Object>of(
                "id", c.getId(),
                "content", c.getContent(),
                "author", c.getAuthor().getEmail(),
                "createdAt", c.getCreatedAt() != null ? c.getCreatedAt() : LocalDateTime.now()
        )).toList();

        return ResponseEntity.ok(ApiResponse.success("Comments retrieved successfully", responseData));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable("commentId") Long commentId,
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        if (!comment.getAuthor().getId().equals(user.getId()) && !"ROLE_ADMIN".equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Not authorized to delete this comment"));
        }

        commentRepository.delete(comment);
        return ResponseEntity.ok(ApiResponse.success("Comment deleted successfully", null));
    }

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Unauthenticated request");
        }
        String principalName = authentication.getName();
        return userRepository.findByEmail(principalName)
                .or(() -> userRepository.findByUsername(principalName))
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principalName));
    }

    private FileUploadResponseDto mapToFileDto(FileMetadata file) {
        return FileUploadResponseDto.builder()
                .id(file.getId())
                .originalName(file.getOriginalName())
                .cloudinaryUrl(file.getCloudinaryUrl())
                .cloudinaryPublicId(file.getCloudinaryPublicId())
                .fileType(file.getContentType())
                .fileSize(file.getFileSize())
                .downloadCount(file.getDownloadCount())
                .uploadedAt(file.getUploadedAt())
                .uploadedBy(file.getUploadedBy() != null ? file.getUploadedBy().getEmail() : null)
                .build();
    }
}
