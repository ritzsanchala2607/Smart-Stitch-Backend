package com.stitcho.beta.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stitcho.beta.dto.ApiResponse;
import com.stitcho.beta.dto.OwnerProfileResponse;
import com.stitcho.beta.dto.UpdateOwnerProfileRequest;
import com.stitcho.beta.service.SecureOwnerService;
import com.stitcho.beta.util.JwtUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 🔐 SECURE OWNER CONTROLLER
 * Owner can view/update their own shop profile
 * No shopId in URL - extracted from JWT
 */
@RestController
@RequestMapping("/api/owners")
@RequiredArgsConstructor
public class SecureOwnerController {
    private final SecureOwnerService ownerService;
    private final JwtUtil jwtUtil;

    @GetMapping("/my-shop")
    public ResponseEntity<ApiResponse<OwnerProfileResponse>> getMyShopProfile(
            @RequestHeader("Authorization") String authHeader) {
        
        String token = jwtUtil.getTokenFromHeader(authHeader);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.success("Invalid or missing token", null));
        }

        Long userId = jwtUtil.extractUserId(token);
        String role = jwtUtil.extractRole(token);
        
        if (!"OWNER".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.success("Only owners can access this endpoint", null));
        }

        OwnerProfileResponse response = ownerService.getMyShopProfile(userId);
        return ResponseEntity.ok(ApiResponse.success("Shop profile fetched successfully", response));
    }

    @PutMapping("/my-shop")
    public ResponseEntity<ApiResponse<OwnerProfileResponse>> updateMyShopProfile(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody UpdateOwnerProfileRequest request) {
        
        String token = jwtUtil.getTokenFromHeader(authHeader);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.success("Invalid or missing token", null));
        }

        Long userId = jwtUtil.extractUserId(token);
        String role = jwtUtil.extractRole(token);
        
        if (!"OWNER".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.success("Only owners can access this endpoint", null));
        }

        OwnerProfileResponse response = ownerService.updateMyShopProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Shop profile updated successfully", response));
    }
}
