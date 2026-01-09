package com.stitcho.beta.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stitcho.beta.dto.AuthResponse;
import com.stitcho.beta.dto.LoginRequest;
import com.stitcho.beta.dto.RegisterRequest;
import com.stitcho.beta.dto.ResetPasswordRequest;
import com.stitcho.beta.service.AuthService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req, HttpServletResponse response) {
        AuthResponse res = authService.register(req);
        
        // Set userId cookie
        ResponseCookie cookie = ResponseCookie.from("userId", String.valueOf(res.getUserId()))
                .httpOnly(false)
                .secure(false)
                .path("/")
                .maxAge(7 * 24 * 60 * 60) // 7 days
                .sameSite("Lax")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req, HttpServletResponse response) {
        AuthResponse res = authService.login(req);
        
        // Set userId cookie
        ResponseCookie cookie = ResponseCookie.from("userId", String.valueOf(res.getUserId()))
                .httpOnly(false)
                .secure(false)
                .path("/")
                .maxAge(7 * 24 * 60 * 60) // 7 days
                .sameSite("Lax")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
        
        return ResponseEntity.ok(res);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        String message = authService.resetPassword(request.getEmail(), request.getNewPassword());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        
        return ResponseEntity.ok(response);
    }
}
