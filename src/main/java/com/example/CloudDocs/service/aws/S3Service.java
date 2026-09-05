package com.example.CloudDocs.service.aws;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service{
    @Value("${aws.bucket.name}")
    private String bucketName;

    private final S3Client s3Client;

    public String uploadFile(MultipartFile file) throws IOException{
        String key=UUID.randomUUID()+"-"+file.getOriginalFilename();

        PutObjectRequest request=PutObjectRequest.builder()
        .bucket(bucketName)
        .key(key)
        .contentType(file.getContentType())
        .build();
        s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

        log.info("S3 upload success: bucket={}, key={}, size={} bytes", bucketName, key, file.getSize());
        return key;
    }
    public byte[] downloadFile(String key) throws IOException{
        GetObjectRequest request=GetObjectRequest.builder()
        .bucket(bucketName)
        .key(key)
        .build();

        try {
            byte[] bytes = s3Client.getObject(request).readAllBytes();
            log.info("S3 download success: bucket={}, key={}, size={} bytes", bucketName, key, bytes.length);
            return bytes;
        } catch (NoSuchKeyException e) {
            log.warn("S3 download miss: key={} not found in bucket={}", key, bucketName);
            throw e;
        } catch (S3Exception e) {
            log.error("S3 download failed: bucket={}, key={}", bucketName, key, e);
            throw e;
        }
    }
    public void deleteFile(String key){
        DeleteObjectRequest request=DeleteObjectRequest.builder()
        .bucket(bucketName)
        .key(key).build();

        s3Client.deleteObject(request);
        log.info("S3 delete success: bucket={}, key={}", bucketName, key);
    }
    public List<String>  listFiles(){
        ListObjectsV2Request request=ListObjectsV2Request.builder()
        .bucket(bucketName).build();
        List<String> keys = s3Client.listObjectsV2(request).contents().stream().map(S3Object::key).toList();
        log.info("S3 list success: bucket={}, count={}", bucketName, keys.size());
        return keys;
    }
}