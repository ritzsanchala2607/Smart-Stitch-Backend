package com.stitcho.beta.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stitcho.beta.dto.AuthResponse;
import com.stitcho.beta.dto.LoginRequest;
import com.stitcho.beta.dto.RegisterRequest;
import com.stitcho.beta.service.AuthService;
import com.stitcho.beta.service.OAuth2Service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final OAuth2Service oAuth2Service;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        AuthResponse res = authService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        AuthResponse res = authService.login(req);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/oauth2/google")
    public ResponseEntity<AuthResponse> googleLogin(@RequestParam String email, @RequestParam String name,
            @RequestParam String googleId, @RequestParam(required = false) String picture) {
        AuthResponse res = oAuth2Service.handleGoogleLogin(email, name, googleId, picture);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/oauth2/success")
    public ResponseEntity<AuthResponse> oAuth2Success(@RequestParam String email, @RequestParam String name,
            @RequestParam String googleId, @RequestParam(required = false) String picture) {
        AuthResponse res = oAuth2Service.handleGoogleLogin(email, name, googleId, picture);
        return ResponseEntity.ok(res);
    }
}
