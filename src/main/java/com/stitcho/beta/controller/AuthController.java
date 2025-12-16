package com.stitcho.beta.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stitcho.beta.dto.AuthResponse;
import com.stitcho.beta.dto.LoginRequest;
import com.stitcho.beta.dto.RegisterRequest;
import com.stitcho.beta.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        AuthResponse res = authService.register(req);
        System.out.println(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        AuthResponse res = authService.login(req);
        return ResponseEntity.ok(res);
    }
}
