package com.stitcho.beta.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stitcho.beta.dto.ApiResponse;
import com.stitcho.beta.dto.UpdateProfileRequest;
import com.stitcho.beta.dto.UserProfileResponse;
import com.stitcho.beta.service.ProfileService;
import com.stitcho.beta.util.JwtUtil;

import lombok.RequiredArgsConstructor;

/**
 * 🔐 SECURE PROFILE CONTROLLER - JWT-BASED EXAMPLE
 * 
 * This controller demonstrates the secure pattern:
 * - No userId in URL
 * - Extracts userId from JWT token
 * - User can only access their own profile
 */
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profileService;
    private final JwtUtil jwtUtil;

    /**
     * Get current user's profile (extracted from JWT)
     * 
     * Example:
     * GET /api/profile
     * Authorization: Bearer <token>
     */
    @GetMapping
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
            @RequestHeader("Authorization") String authHeader) {
        
        String token = jwtUtil.getTokenFromHeader(authHeader);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.success("Invalid or missing token", null));
        }

        Long userId = jwtUtil.extractUserId(token);
        UserProfileResponse profile = profileService.getUserProfile(userId);
        
        return ResponseEntity.ok(ApiResponse.success("Profile fetched successfully", profile));
    }

    /**
     * Update current user's profile
     * 
     * Example:
     * PUT /api/profile
     * Authorization: Bearer <token>
     * Body: { "name": "New Name", "contactNumber": "1234567890" }
     */
    @PutMapping
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody UpdateProfileRequest request) {
        
        String token = jwtUtil.getTokenFromHeader(authHeader);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.success("Invalid or missing token", null));
        }

        Long userId = jwtUtil.extractUserId(token);
        UserProfileResponse profile = profileService.updateUserProfile(userId, request);
        
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", profile));
    }
}
