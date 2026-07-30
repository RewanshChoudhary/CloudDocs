package com.example.CloudDocs.controller.video;

import com.example.CloudDocs.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/videos")
public class VideoController {

    @PostMapping("/presigned")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createPresignedUpload(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listVideos(Authentication authentication) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getVideo(
            @PathVariable("id") UUID id,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @PostMapping("/{id}/processing")
    public ResponseEntity<ApiResponse<Void>> markProcessing(
            @PathVariable("id") UUID id,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @PostMapping("/{id}/completed")
    public ResponseEntity<ApiResponse<Void>> markCompleted(
            @PathVariable("id") UUID id,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @PostMapping("/{id}/failed")
    public ResponseEntity<ApiResponse<Void>> markFailed(
            @PathVariable("id") UUID id,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
