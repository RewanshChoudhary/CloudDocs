package com.example.CloudDocs.controller.video;

import com.example.CloudDocs.dto.ApiResponse;
import com.example.CloudDocs.dto.video.VideoCompletedRequest;
import com.example.CloudDocs.dto.video.VideoFailedRequest;
import com.example.CloudDocs.dto.video.VideoResponse;
import com.example.CloudDocs.dto.video.VideoUploadRequest;
import com.example.CloudDocs.dto.video.VideoUploadResponse;
import com.example.CloudDocs.model.User;
import com.example.CloudDocs.service.video.VideoService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    @PostMapping("/presigned")
    public ResponseEntity<ApiResponse<VideoUploadResponse>> createPresignedUpload(
            @Valid @RequestBody VideoUploadRequest request,
            Authentication authentication) {
        VideoUploadResponse response = videoService.createPresignedUpload(getAuthenticatedUser(authentication), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Presigned upload URL created", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<VideoResponse>>> listVideos(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(
                "Videos retrieved successfully",
                videoService.listVideos(getAuthenticatedUser(authentication))
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VideoResponse>> getVideo(
            @PathVariable("id") UUID id,
            Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(
                "Video retrieved successfully",
                videoService.getVideo(getAuthenticatedUser(authentication), id)
        ));
    }

    @PostMapping("/{id}/processing")
    public ResponseEntity<ApiResponse<Void>> markProcessing(
            @PathVariable("id") UUID id,
            Authentication authentication) {
        videoService.markProcessing(getAuthenticatedUser(authentication), id);
        return ResponseEntity.ok(ApiResponse.success("Video marked processing", null));
    }

    @PostMapping("/{id}/completed")
    public ResponseEntity<ApiResponse<Void>> markCompleted(
            @PathVariable("id") UUID id,
            @RequestBody(required = false) VideoCompletedRequest request,
            Authentication authentication) {
        videoService.markCompleted(
                getAuthenticatedUser(authentication),
                id,
                request == null ? new VideoCompletedRequest(null, null, null, null) : request
        );
        return ResponseEntity.ok(ApiResponse.success("Video marked completed", null));
    }

    @PostMapping("/{id}/failed")
    public ResponseEntity<ApiResponse<Void>> markFailed(
            @PathVariable("id") UUID id,
            @RequestBody(required = false) VideoFailedRequest request,
            Authentication authentication) {
        videoService.markFailed(
                getAuthenticatedUser(authentication),
                id,
                request == null ? new VideoFailedRequest(null) : request
        );
        return ResponseEntity.ok(ApiResponse.success("Video marked failed", null));
    }

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return user;
    }
}
