package com.stitcho.beta.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stitcho.beta.dto.ApiResponse;
import com.stitcho.beta.dto.MeasurementProfileRequest;
import com.stitcho.beta.dto.MeasurementProfileResponse;
import com.stitcho.beta.entity.DressType;
import com.stitcho.beta.service.MeasurementService;
import com.stitcho.beta.util.JwtUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 🔐 SECURE MEASUREMENT CONTROLLER
 * Manages measurement profiles for different dress types
 * Owner can manage customer measurements
 */
@RestController
@RequestMapping("/api/measurements")
@RequiredArgsConstructor
public class MeasurementController {
    
    private final MeasurementService measurementService;
    private final JwtUtil jwtUtil;

    /**
     * Create measurement profile for a customer
     * POST /api/measurements
     */
    @PostMapping
    public ResponseEntity<ApiResponse<MeasurementProfileResponse>> createProfile(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody MeasurementProfileRequest request) {
        
        String token = jwtUtil.getTokenFromHeader(authHeader);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.success("Invalid or missing token", null));
        }

        String role = jwtUtil.extractRole(token);
        if (!"OWNER".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.success("Only owners can manage measurements", null));
        }

        MeasurementProfileResponse response = measurementService.createProfile(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Measurement profile created successfully", response));
    }

    /**
     * Get all measurement profiles for a customer by userId
     * GET /api/measurements/customer/{userId}
     * Access: Owner (all customers) or Customer (their own only)
     */
    @GetMapping("/customer/{userId}")
    public ResponseEntity<ApiResponse<List<MeasurementProfileResponse>>> getCustomerProfiles(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long userId) {
        
        String token = jwtUtil.getTokenFromHeader(authHeader);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.success("Invalid or missing token", null));
        }

        String role = jwtUtil.extractRole(token);
        
        // If customer, verify they're accessing their own measurements
        if ("CUSTOMER".equalsIgnoreCase(role)) {
            Long tokenUserId = jwtUtil.extractUserId(token);
            if (tokenUserId == null || !tokenUserId.equals(userId)) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.success("Customers can only view their own measurements", null));
            }
        }

        List<MeasurementProfileResponse> responses = measurementService.getAllProfilesForCustomerByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("Measurement profiles fetched successfully", responses));
    }

    /**
     * Get specific measurement profile by ID
     * GET /api/measurements/{profileId}
     * Access: Owner (all profiles) or Customer (their own only)
     */
    @GetMapping("/{profileId}")
    public ResponseEntity<ApiResponse<MeasurementProfileResponse>> getProfileById(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long profileId) {
        
        String token = jwtUtil.getTokenFromHeader(authHeader);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.success("Invalid or missing token", null));
        }

        String role = jwtUtil.extractRole(token);
        MeasurementProfileResponse response = measurementService.getProfileById(profileId);
        
        // If customer, verify they're accessing their own measurements
        if ("CUSTOMER".equalsIgnoreCase(role)) {
            Long tokenCustomerId = jwtUtil.extractCustomerId(token);
            if (tokenCustomerId == null || !tokenCustomerId.equals(response.getCustomerId())) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.success("Customers can only view their own measurements", null));
            }
        }

        return ResponseEntity.ok(ApiResponse.success("Measurement profile fetched successfully", response));
    }

    /**
     * Get measurement profile by dress type using userId
     * GET /api/measurements/customer/{userId}/dress-type/{dressType}
     * Access: Owner (all customers) or Customer (their own only)
     */
    @GetMapping("/customer/{userId}/dress-type/{dressType}")
    public ResponseEntity<ApiResponse<MeasurementProfileResponse>> getProfileByDressType(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long userId,
            @PathVariable DressType dressType) {
        
        String token = jwtUtil.getTokenFromHeader(authHeader);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.success("Invalid or missing token", null));
        }

        String role = jwtUtil.extractRole(token);
        
        // If customer, verify they're accessing their own measurements
        if ("CUSTOMER".equalsIgnoreCase(role)) {
            Long tokenUserId = jwtUtil.extractUserId(token);
            if (tokenUserId == null || !tokenUserId.equals(userId)) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.success("Customers can only view their own measurements", null));
            }
        }

        MeasurementProfileResponse response = measurementService.getProfileByUserId(userId, dressType);
        return ResponseEntity.ok(ApiResponse.success("Measurement profile fetched successfully", response));
    }

    /**
     * Get my measurement profiles (Customer convenience endpoint)
     * GET /api/measurements/me
     * Access: Customer only
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<MeasurementProfileResponse>>> getMyProfiles(
            @RequestHeader("Authorization") String authHeader) {
        
        String token = jwtUtil.getTokenFromHeader(authHeader);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.success("Invalid or missing token", null));
        }

        String role = jwtUtil.extractRole(token);
        if (!"CUSTOMER".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.success("Only customers can use this endpoint", null));
        }

        Long customerId = jwtUtil.extractCustomerId(token);
        if (customerId == null) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.success("Customer ID not found in token", null));
        }

        List<MeasurementProfileResponse> responses = measurementService.getAllProfilesForCustomer(customerId);
        return ResponseEntity.ok(ApiResponse.success("Your measurement profiles fetched successfully", responses));
    }

    /**
     * Update measurement profile
     * PUT /api/measurements/{profileId}
     */
    @PutMapping("/{profileId}")
    public ResponseEntity<ApiResponse<MeasurementProfileResponse>> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long profileId,
            @Valid @RequestBody MeasurementProfileRequest request) {
        
        String token = jwtUtil.getTokenFromHeader(authHeader);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.success("Invalid or missing token", null));
        }

        String role = jwtUtil.extractRole(token);
        if (!"OWNER".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.success("Only owners can update measurements", null));
        }

        MeasurementProfileResponse response = measurementService.updateProfile(profileId, request);
        return ResponseEntity.ok(ApiResponse.success("Measurement profile updated successfully", response));
    }

    /**
     * Delete measurement profile
     * DELETE /api/measurements/{profileId}
     */
    @DeleteMapping("/{profileId}")
    public ResponseEntity<ApiResponse<Void>> deleteProfileById(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long profileId) {
        
        String token = jwtUtil.getTokenFromHeader(authHeader);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.success("Invalid or missing token", null));
        }

        String role = jwtUtil.extractRole(token);
        if (!"OWNER".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.success("Only owners can delete measurements", null));
        }

        measurementService.deleteProfileById(profileId);
        return ResponseEntity.ok(ApiResponse.success("Measurement profile deleted successfully"));
    }
}
