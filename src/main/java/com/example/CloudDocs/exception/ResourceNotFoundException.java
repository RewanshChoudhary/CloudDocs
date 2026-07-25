package com.example.CloudDocs.exception;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ResourceNotFoundException extends Exception{
   public ResourceNotFoundException(String message) {
        super(message);

    }

}
