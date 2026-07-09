package com.example.CloudDocs.repository;

import com.example.CloudDocs.model.FileMetadata;
import com.example.CloudDocs.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
    List<FileMetadata> findByUploadedBy(User user);
    List<FileMetadata> findByOriginalNameContainingIgnoreCase(String name);
}