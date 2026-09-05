package com.example.CloudDocs.service;

import java.io.IOException;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Service
@RequiredArgsConstructor
@Slf4j
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
        log.info("Cloudinary upload success: publicId={}, size={} bytes", publicId, file.getSize());
        return Map.of(
                "url", url,
                "publicId", publicId
        );
    }

    public void deleteFile(String publicId) throws IOException {
        if (publicId != null && !publicId.isBlank()) {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Cloudinary delete success: publicId={}", publicId);
        } else {
            log.warn("Cloudinary delete skipped: empty publicId");
        }
    }
}
