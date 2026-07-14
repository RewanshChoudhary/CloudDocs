package com.example.CloudDocs.exception;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DuplicateEmailException extends RuntimeException{
    public DuplicateEmailException(String message) {
        super(message);

    }
}
