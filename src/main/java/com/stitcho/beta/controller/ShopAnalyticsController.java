package com.stitcho.beta.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.stitcho.beta.dto.ApiResponse;
import com.stitcho.beta.dto.ShopAnalyticsResponse;
import com.stitcho.beta.service.SecureOwnerService;
import com.stitcho.beta.util.JwtUtil;

import lombok.RequiredArgsConstructor;

/**
 * 🔐 SHOP ANALYTICS CONTROLLER
 * Owner can view shop analytics and dashboard statistics
 */
@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.OPTIONS})
public class ShopAnalyticsController {
    private final SecureOwnerService ownerService;
    private final JwtUtil jwtUtil;

    /**
     * Get shop analytics for owner dashboard
     * GET /api/shops/me/analytics
     */
    @GetMapping("/me/analytics")
    public ResponseEntity<ApiResponse<ShopAnalyticsResponse>> getShopAnalytics(
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

        ShopAnalyticsResponse analytics = ownerService.getShopAnalytics(userId);
        return ResponseEntity.ok(ApiResponse.success("Shop analytics fetched successfully", analytics));
    }
}
