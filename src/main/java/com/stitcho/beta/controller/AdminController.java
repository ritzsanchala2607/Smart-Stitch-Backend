package com.stitcho.beta.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stitcho.beta.dto.AdminDashboardResponse;
import com.stitcho.beta.dto.AllShopsResponse;
import com.stitcho.beta.dto.ApiResponse;
import com.stitcho.beta.dto.PlatformAnalyticsResponse;
import com.stitcho.beta.dto.ShopAnalyticsResponse;
import com.stitcho.beta.dto.UpdateShopRequest;
import com.stitcho.beta.service.AdminService;
import com.stitcho.beta.util.JwtUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 🔐 ADMIN CONTROLLER
 * Admin can view platform-wide analytics and manage all shops
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;
    private final JwtUtil jwtUtil;

    /**
     * Get admin dashboard overview
     * Shows total shops, owners, workers, orders, active shops, and growth
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getAdminDashboard(
            @RequestHeader("Authorization") String authHeader) {
        
        String token = jwtUtil.getTokenFromHeader(authHeader);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.success("Invalid or missing token", null));
        }

        String role = jwtUtil.extractRole(token);
        
        if (!"ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.success("Only admins can access this endpoint", null));
        }

        AdminDashboardResponse dashboard = adminService.getAdminDashboard();
        return ResponseEntity.ok(ApiResponse.success("Admin dashboard fetched successfully", dashboard));
    }

    /**
     * Get platform analytics
     * Shows system metrics, orders vs shops growth, and monthly active users
     */
    @GetMapping("/analytics/platform")
    public ResponseEntity<ApiResponse<PlatformAnalyticsResponse>> getPlatformAnalytics(
            @RequestHeader("Authorization") String authHeader) {
        
        String token = jwtUtil.getTokenFromHeader(authHeader);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.success("Invalid or missing token", null));
        }

        String role = jwtUtil.extractRole(token);
        
        if (!"ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.success("Only admins can access this endpoint", null));
        }

        PlatformAnalyticsResponse analytics = adminService.getPlatformAnalytics();
        return ResponseEntity.ok(ApiResponse.success("Platform analytics fetched successfully", analytics));
    }

    /**
     * Get shop analytics
     * Shows shop status distribution, workers distribution, and monthly trends
     */
    @GetMapping("/analytics/shops")
    public ResponseEntity<ApiResponse<ShopAnalyticsResponse>> getShopAnalytics(
            @RequestHeader("Authorization") String authHeader) {
        
        String token = jwtUtil.getTokenFromHeader(authHeader);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.success("Invalid or missing token", null));
        }

        String role = jwtUtil.extractRole(token);
        
        if (!"ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.success("Only admins can access this endpoint", null));
        }

        ShopAnalyticsResponse analytics = adminService.getShopAnalytics();
        return ResponseEntity.ok(ApiResponse.success("Shop analytics fetched successfully", analytics));
    }

    /**
     * Get all shops with details
     * Shows shop name, owner info, total orders, workers, and status
     */
    @GetMapping("/shops")
    public ResponseEntity<ApiResponse<List<AllShopsResponse>>> getAllShops(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String search) {
        
        String token = jwtUtil.getTokenFromHeader(authHeader);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.success("Invalid or missing token", null));
        }

        String role = jwtUtil.extractRole(token);
        
        if (!"ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.success("Only admins can access this endpoint", null));
        }

        List<AllShopsResponse> shops = adminService.getAllShops(search);
        return ResponseEntity.ok(ApiResponse.success("Shops fetched successfully", shops));
    }

    /**
     * Update shop details
     * Allows admin to edit shop and owner information
     */
    @PutMapping("/shops/{shopId}")
    public ResponseEntity<ApiResponse<Void>> updateShop(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long shopId,
            @Valid @RequestBody UpdateShopRequest request) {
        
        String token = jwtUtil.getTokenFromHeader(authHeader);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.success("Invalid or missing token", null));
        }

        String role = jwtUtil.extractRole(token);
        
        if (!"ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.success("Only admins can update shops", null));
        }

        adminService.updateShop(shopId, request);
        return ResponseEntity.ok(ApiResponse.success("Shop updated successfully"));
    }

    /**
     * Delete shop and all related data
     * Cascade deletes: customers, workers, orders, tasks, measurements, ratings, owner
     */
    @DeleteMapping("/shops/{shopId}")
    public ResponseEntity<ApiResponse<Void>> deleteShop(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long shopId) {
        
        String token = jwtUtil.getTokenFromHeader(authHeader);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.success("Invalid or missing token", null));
        }

        String role = jwtUtil.extractRole(token);
        
        if (!"ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.success("Only admins can delete shops", null));
        }

        adminService.deleteShop(shopId);
        return ResponseEntity.ok(ApiResponse.success("Shop and all related data deleted successfully"));
    }
}