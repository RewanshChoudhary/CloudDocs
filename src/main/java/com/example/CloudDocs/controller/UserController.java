package com.example.CloudDocs.controller;

import com.example.CloudDocs.dto.AuthResponse;
import com.example.CloudDocs.dto.LoginRequest;
import com.example.CloudDocs.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
    private final AuthenticationManager authenticationManager;
private final JwtService jwtService;

    @PostMapping
    public ResponseEntity<AuthResponse> userLogin(@RequestBody LoginRequest loginRequest){
        Authentication auth=authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(),
                loginRequest.getPassword()
        ));
        UserDetails user=(UserDetails)auth.getPrincipal();
        assert user != null;
        String token=jwtService.generateToken(user);
        return ResponseEntity.ok(new AuthResponse(token));


    }
}
