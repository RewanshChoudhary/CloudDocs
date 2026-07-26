package com.example.CloudDocs.controller;

import com.example.CloudDocs.dto.ApiResponse;
import com.example.CloudDocs.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/media")
public class MediaController {

    private final CloudinaryService cloudinaryService;

    public ResponseEntity<ApiResponse> uploadFile(@RequestParam("file")MultipartFile file, @RequestParam(value = "description",required = false) String description) throws IOException {
        if (file.isEmpty()){
            return ResponseEntity.badRequest().body(ApiResponse.error("File is empty"));

        }
        cloudinaryService.uploadFile(file,description);
        return ResponseEntity.ok(ApiResponse.success("File uploaded successfully"));

    }
public ResponseEntity<ApiResponse> deleteFile(@RequestParam("publicId") String publicId) throws IOException {
    cloudinaryService.deleteFile(publicId);
    return ResponseEntity.ok(ApiResponse.success("File deleted successfully"));

}
}
