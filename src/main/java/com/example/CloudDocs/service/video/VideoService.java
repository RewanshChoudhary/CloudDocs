package com.example.CloudDocs.service.video;

import com.example.CloudDocs.model.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class VideoService {

    public Map<String, Object> createPresignedUpload(User user, Map<String, Object> request) {
        return null;
    }

    public List<Map<String, Object>> listVideos(User user) {
        return null;
    }

    public Map<String, Object> getVideo(User user, UUID videoId) {
        return null;
    }

    public String createUploadUrl(String bucket, String key, String contentType) {
        return null;
    }

    public String createDownloadUrl(String bucket, String key) {
        return null;
    }

    public String buildRawKey(UUID videoId, String filename) {
        return null;
    }

    public String buildProcessedKey(UUID videoId, String rendition) {
        return null;
    }

    public String buildThumbnailKey(UUID videoId) {
        return null;
    }
}
