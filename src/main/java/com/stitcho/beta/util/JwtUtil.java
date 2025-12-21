package com.stitcho.beta.util;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    @Value("${jwt.secret:mySecretKeyForJWTTokenGenerationAndValidationPurpose12345}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    public String generateToken(String email, String name, String role) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        
        return Jwts.builder()
                .subject(email)
                .claim("name", name)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(key)
                .compact();
    }

    public String generateTokenWithIds(String email, String name, String role, Long userId, Long shopId, Long customerId, Long workerId) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        
        var builder = Jwts.builder()
                .subject(email)
                .claim("name", name)
                .claim("role", role)
                .claim("userId", userId);
        
        if (shopId != null) builder.claim("shopId", shopId);
        if (customerId != null) builder.claim("customerId", customerId);
        if (workerId != null) builder.claim("workerId", workerId);
        
        return builder
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(key)
                .compact();
    }

    public Claims extractAllClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        Object userId = extractAllClaims(token).get("userId");
        return userId != null ? ((Number) userId).longValue() : null;
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public Long extractShopId(String token) {
        Object shopId = extractAllClaims(token).get("shopId");
        return shopId != null ? ((Number) shopId).longValue() : null;
    }

    public Long extractCustomerId(String token) {
        Object customerId = extractAllClaims(token).get("customerId");
        return customerId != null ? ((Number) customerId).longValue() : null;
    }

    public Long extractWorkerId(String token) {
        Object workerId = extractAllClaims(token).get("workerId");
        return workerId != null ? ((Number) workerId).longValue() : null;
    }

    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
