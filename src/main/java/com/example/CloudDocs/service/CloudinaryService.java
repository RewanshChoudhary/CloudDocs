package com.example.CloudDocs.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {
    private final Cloudinary cloudinary;


    public Map<String,Object> uploadFile(MultipartFile file,String descripttion) throws IOException {
        Map uploadRes=cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap("folder","clouddocs/","description",descripttion)

        );
        return Map.of(
                "url",uploadRes.get("secure_url"),"publicId",uploadRes.get("public_id")
        );


    }
    public void deleteFile(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId,ObjectUtils.emptyMap());
        System.out.println("File deleted successfully");
        
    }
}
