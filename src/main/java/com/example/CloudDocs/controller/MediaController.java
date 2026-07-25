package com.example.CloudDocs.controller;

import com.example.CloudDocs.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/media")
public class MediaController {

    private final CloudinaryService cloudinaryService;

    public ResponseEntity<ApiResponse> uploadFile(@RequestParam("file")MultipartFile file, @RequestParam(value = "description",required = false) String description){
        if (file.isEmpty()){
            return ResponseEntity.badRequest().body(ApiResponse.error("File is empty"));

        }
        cloudinaryService.uploadFile(file,description);
        return ResponseEntity.ok(ApiResponse.success("File uploaded successfully"));

    }
}
