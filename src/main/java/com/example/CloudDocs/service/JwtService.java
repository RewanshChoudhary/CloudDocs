package com.example.CloudDocs.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {
    @Value("${jwt.secret}")
    private  String secretKey;

    @Value("${jwt.expiration}")
    private Long expiration;

    public String generateToken(UserDetails userDetails){
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("roles",userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                .signWith(getSigningKey())
                .compact();

    }
    public Claims extractDetails(String token){
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

    }
private SecretKey getSigningKey(){
        return Keys.hmacShaKeyFor(secretKey.getBytes());

}
private boolean isExpired(String token){
        return extractDetails(token).getExpiration().before(new Date(expiration));

}
public String extractUsername(String token){
        return extractDetails(token).getSubject();

}
public boolean isTokenValid(String token,UserDetails user){
        String username=extractUsername(token);
        return username.equals(user.getUsername()) && !isExpired(token);



}
}
