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


    public Map<String, Object> uploadFile(MultipartFile file, String description) throws IOException {
        Map uploadRes = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", "clouddocs/",
                        "resource_type", "auto",
                        "description", description != null ? description : ""
                )
        );
        String url = uploadRes.get("secure_url") != null ? uploadRes.get("secure_url").toString() : "";
        String publicId = uploadRes.get("public_id") != null ? uploadRes.get("public_id").toString() : "";
        return Map.of(
                "url", url,
                "publicId", publicId
        );
    }

    public void deleteFile(String publicId) throws IOException {
        if (publicId != null && !publicId.isBlank()) {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        }
    }
}
