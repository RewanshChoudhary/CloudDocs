package com.example.CloudDocs.service.aws;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;
/*
General structure of builder and verbs */
@Service
@RequiredArgsConstructor
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

 
        return key;


    }
    public byte[] downloadFile(String key) throws IOException{
        GetObjectRequest request=GetObjectRequest.builder()
        .bucket(bucketName)
        .key(key)
        .build();

        return s3Client.getObject(request).readAllBytes();


    }
    public void deleteFile(String key){
        DeleteObjectRequest request=DeleteObjectRequest.builder()
        .bucket(bucketName)
        .key(key).build();

        s3Client.deleteObject(request);

    }
    public List<String>  listFiles(){
        ListObjectsV2Request request=ListObjectsV2Request.builder()
        .bucket(bucketName).build();
        return s3Client.listObjectsV2(request).contents().stream().map(S3Object::key).toList();
        
    }
    
}