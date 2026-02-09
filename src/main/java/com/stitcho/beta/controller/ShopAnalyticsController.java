package com.stitcho.beta.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stitcho.beta.dto.ApiResponse;
import com.stitcho.beta.dto.ShopAnalyticsResponse;
import com.stitcho.beta.dto.MonthlyRevenueResponse;
import com.stitcho.beta.dto.CalendarTaskResponse;
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

    /**
     * Get monthly revenue breakdown
     * GET /api/shops/me/revenue/monthly
     * Optional query param: year (defaults to current year)
     */
    @GetMapping("/me/revenue/monthly")
    public ResponseEntity<ApiResponse<MonthlyRevenueResponse>> getMonthlyRevenue(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) Integer year) {
        
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

        MonthlyRevenueResponse revenue = ownerService.getMonthlyRevenue(userId, year);
        return ResponseEntity.ok(ApiResponse.success("Monthly revenue fetched successfully", revenue));
    }

    /**
     * Get calendar tasks organized by due date
     * GET /api/shops/me/tasks/calendar
     * Query params: year (optional), month (optional)
     */
    @GetMapping("/me/tasks/calendar")
    public ResponseEntity<ApiResponse<java.util.List<CalendarTaskResponse>>> getCalendarTasks(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        
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

        java.util.List<CalendarTaskResponse> calendarTasks = ownerService.getCalendarTasks(userId, year, month);
        return ResponseEntity.ok(ApiResponse.success("Calendar tasks fetched successfully", calendarTasks));
    }
}
