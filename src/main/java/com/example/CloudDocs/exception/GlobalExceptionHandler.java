package com.example.CloudDocs.exception;

import com.example.CloudDocs.dto.ApiResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor

public class GlobalExceptionHandler {
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiResponse> handleDuplicateEmailException(DuplicateEmailException e){
        return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));



    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,String>> handleValidation(MethodArgumentNotValidException e){
       Map<String ,String > errors=new HashMap<>();
       e.getBindingResult()
               .getFieldErrors()
               .forEach(err->errors.put(err.getField(),err.getDefaultMessage()));
       return ResponseEntity.badRequest().body(errors);


    }
    @ExceptionHandler(ResourceNotFoundException.class )
    public ResponseEntity<ApiResponse> handleResourceNotFoundException(ResourceNotFoundException e){
        return  ResponseEntity.notFound().build();



    }
}
