package com.example.CloudDocs.controller.aws;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.CloudDocs.dto.ApiResponse;
import com.example.CloudDocs.service.aws.S3Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/aws/file")
public class AwsFileController {
    private final S3Service s3Service;

    @GetMapping("/download/{key}")
    public ResponseEntity<ApiResponse<Map<String,Object>>> downloadFile(@PathVariable String key) throws IOException {
        log.info("S3 download requested: key={}", key);
        byte[] fileBytes = s3Service.downloadFile(key);

        Map<String, Object> data = new HashMap<>();
        data.put("key", key);
        data.put("content", Base64.getEncoder().encodeToString(fileBytes));
        data.put("size", fileBytes.length);

        return ResponseEntity.ok(ApiResponse.success("File downloaded successfully", data));
    }
    @DeleteMapping("/delete/{key}")
    public ResponseEntity<Void> deleteFile(@PathVariable String key ){
        log.info("S3 delete requested: key={}", key);
        s3Service.deleteFile(key);
        return ResponseEntity.noContent().build();


    }

    @PostMapping("/upload/{key}")
    public ResponseEntity<String> uploadFile(@RequestParam MultipartFile file) throws IOException{
        
       String key=s3Service.uploadFile(file);
        return ResponseEntity.ok(key);



    }
      @GetMapping
    public List<String> list() {
        return s3Service.listFiles();
    }
}
