package com.example.CloudDocs.controller;

import com.example.CloudDocs.dto.AuthResponse;
import com.example.CloudDocs.dto.LoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
    private final AuthenticationManager authenticationManager;


    @PostMapping
    public ResponseEntity<AuthResponse> userLogin(@RequestBody LoginRequest loginRequest){
        Authentication auth=authe

    }
}
