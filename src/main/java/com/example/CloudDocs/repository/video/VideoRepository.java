package com.example.CloudDocs.repository.video;

import com.example.CloudDocs.model.User;
import com.example.CloudDocs.model.video.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VideoRepository extends JpaRepository<Video, UUID> {
    List<Video> findByUserOrderByCreatedAtDesc(User user);
}
