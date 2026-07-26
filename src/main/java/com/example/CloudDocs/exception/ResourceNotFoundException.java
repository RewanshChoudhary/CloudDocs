package com.example.CloudDocs.exception;

import lombok.RequiredArgsConstructor;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
