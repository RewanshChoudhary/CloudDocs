package com.example.CloudDocs.repository;

import com.example.CloudDocs.model.Comment;
import com.example.CloudDocs.model.FileMetadata;
import com.example.CloudDocs.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByFile(FileMetadata file);
    List<Comment> findByAuthor(User author);
}