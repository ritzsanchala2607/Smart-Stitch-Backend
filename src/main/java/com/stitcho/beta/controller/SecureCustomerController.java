package com.stitcho.beta.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stitcho.beta.dto.ApiResponse;
import com.stitcho.beta.dto.CreateCustomerRequest;
import com.stitcho.beta.dto.CreateCustomerResponse;
import com.stitcho.beta.dto.CustomerResponse;
import com.stitcho.beta.dto.CustomerStatsResponse;
import com.stitcho.beta.dto.OrderResponse;
import com.stitcho.beta.dto.PaymentHistoryResponse;
import com.stitcho.beta.dto.ShopInfoResponse;
import com.stitcho.beta.dto.UpdateCustomerRequest;
import com.stitcho.beta.service.SecureCustomerService;
import com.stitcho.beta.util.JwtUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 🔐 SECURE CUSTOMER CONTROLLER
 * Owner can manage customers in their shop
 * Customer can view/update their own profile
 */
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class SecureCustomerController {
    private final SecureCustomerService customerService;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<ApiResponse<CreateCustomerResponse>> createCustomer(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody CreateCustomerRequest request) {
        
        String token = jwtUtil.getTokenFromHeader(authHeader);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.success("Invalid or missing token", null));
        }

        Long userId = jwtUtil.extractUserId(token);
        String role = jwtUtil.extractRole(token);
        
        // Debug: Log the role for troubleshooting
        System.out.println("DEBUG - User ID: " + userId + ", Role from JWT: '" + role + "'");
        
        if (!"OWNER".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.success("Access denied. Please ensure you are logged in as a shop owner. Your role: " + role, null));
        }

        CreateCustomerResponse response = customerService.createCustomer(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Customer created successfully", response));
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomer(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long customerId) {
        
        String token = jwtUtil.getTokenFromHeader(authHeader);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.success("Invalid or missing token", null));
        }

        Long userId = jwtUtil.extractUserId(token);
        String role = jwtUtil.extractRole(token);

        CustomerResponse response = customerService.getCustomer(userId, role, customerId);
        return ResponseEntity.ok(ApiResponse.success("Customer fetched successfully", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<CustomerResponse>> getMyProfile(
            @RequestHeader("Authorization") String authHeader) {
        
        String token = jwtUtil.getTokenFromHeader(authHeader);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.success("Invalid or missing token", null));
        }

        Long userId = jwtUtil.extractUserId(token);
        String role = jwtUtil.extractRole(token);
        
        if (!"CUSTOMER".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.success("Only customers can access this endpoint", null));
        }

        CustomerResponse response = customerService.getMyProfile(userId);
        return ResponseEntity.ok(ApiResponse.success("Profile fetched successfully", response));
    }

    @PutMapping("/{customerId}")
    public ResponseEntity<ApiResponse<Void>> updateCustomer(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long customerId,
            @RequestBody UpdateCustomerRequest request) {
        
        String token = jwtUtil.getTokenFromHeader(authHeader);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.success("Invalid or missing token", null));
        }

        Long userId = jwtUtil.extractUserId(token);
        String role = jwtUtil.extractRole(token);

        customerService.updateCustomer(userId, role, customerId, request);
        return ResponseEntity.ok(ApiResponse.success("Customer updated successfully"));
    }

    @GetMapping("/me/orders")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(
            @RequestHeader("Authorization") String authHeader) {
        
        String token = jwtUtil.getTokenFromHeader(authHeader);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.success("Invalid or missing token", null));
        }

        Long userId = jwtUtil.extractUserId(token);
        String role = jwtUtil.extractRole(token);
        
        if (!"CUSTOMER".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.success("Only customers can access this endpoint", null));
        }

        List<OrderResponse> orders = customerService.getMyOrders(userId);
        return ResponseEntity.ok(ApiResponse.success("Orders fetched successfully", orders));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> getAllCustomers(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String name) {
        
        String token = jwtUtil.getTokenFromHeader(authHeader);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.success("Invalid or missing token", null));
        }

        Long userId = jwtUtil.extractUserId(token);
        String role = jwtUtil.extractRole(token);
        
        if (!"OWNER".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.success("Only owners can view all customers", null));
        }

        List<CustomerResponse> customers = customerService.getAllCustomers(userId, name);
        return ResponseEntity.ok(ApiResponse.success("Customers fetched successfully", customers));
    }

    @DeleteMapping("/{customerId}")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long customerId) {
        
        String token = jwtUtil.getTokenFromHeader(authHeader);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.success("Invalid or missing token", null));
        }

        Long userId = jwtUtil.extractUserId(token);
        String role = jwtUtil.extractRole(token);
        
        if (!"OWNER".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.success("Only owners can delete customers", null));
        }

        customerService.deleteCustomer(userId, customerId);
        return ResponseEntity.ok(ApiResponse.success("Customer deleted successfully"));
    }

    // ==================== CUSTOMER DASHBOARD ENDPOINTS ====================

    /**
     * Get customer dashboard statistics
     * GET /api/customers/me/stats
     */
    @GetMapping("/me/stats")
    public ResponseEntity<ApiResponse<CustomerStatsResponse>> getCustomerStats(
            @RequestHeader("Authorization") String authHeader) {
        
        String token = jwtUtil.getTokenFromHeader(authHeader);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.success("Invalid or missing token", null));
        }

        Long userId = jwtUtil.extractUserId(token);
        String role = jwtUtil.extractRole(token);
        
        if (!"CUSTOMER".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.success("Only customers can access this endpoint", null));
        }

        CustomerStatsResponse stats = customerService.getCustomerStats(userId);
        return ResponseEntity.ok(ApiResponse.success("Customer statistics fetched successfully", stats));
    }

    /**
     * Get customer payment history
     * GET /api/customers/me/payments
     */
    @GetMapping("/me/payments")
    public ResponseEntity<ApiResponse<List<PaymentHistoryResponse>>> getPaymentHistory(
            @RequestHeader("Authorization") String authHeader) {
        
        String token = jwtUtil.getTokenFromHeader(authHeader);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.success("Invalid or missing token", null));
        }

        Long userId = jwtUtil.extractUserId(token);
        String role = jwtUtil.extractRole(token);
        
        if (!"CUSTOMER".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.success("Only customers can access this endpoint", null));
        }

        List<PaymentHistoryResponse> payments = customerService.getPaymentHistory(userId);
        return ResponseEntity.ok(ApiResponse.success("Payment history fetched successfully", payments));
    }

    /**
     * Get customer order history with date filtering
     * GET /api/customers/me/orders/history
     */
    @GetMapping("/me/orders/history")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrderHistory(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        String token = jwtUtil.getTokenFromHeader(authHeader);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.success("Invalid or missing token", null));
        }

        Long userId = jwtUtil.extractUserId(token);
        String role = jwtUtil.extractRole(token);
        
        if (!"CUSTOMER".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.success("Only customers can access this endpoint", null));
        }

        List<OrderResponse> orders = customerService.getOrderHistory(userId, year, month, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Order history fetched successfully", orders));
    }

    /**
     * Get recent activities for customer dashboard
     * GET /api/customers/me/activities
     */
    @GetMapping("/me/activities")
    public ResponseEntity<ApiResponse<List<com.stitcho.beta.dto.RecentActivityResponse>>> getRecentActivities(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        
        String token = jwtUtil.getTokenFromHeader(authHeader);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.success("Invalid or missing token", null));
        }

        Long userId = jwtUtil.extractUserId(token);
        String role = jwtUtil.extractRole(token);
        
        if (!"CUSTOMER".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.success("Only customers can access this endpoint", null));
        }

        List<com.stitcho.beta.dto.RecentActivityResponse> activities = customerService.getRecentActivities(userId, limit);
        return ResponseEntity.ok(ApiResponse.success("Recent activities fetched successfully", activities));
    }
}

/**
 * 🔐 SHOP INFO CONTROLLER
 * Public endpoint for shop information
 */
@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.OPTIONS})
class ShopInfoController {
    private final SecureCustomerService customerService;
    private final JwtUtil jwtUtil;

    /**
     * Get shop information with ratings
     * GET /api/shops/{shopId}
     */
    @GetMapping("/{shopId}")
    public ResponseEntity<ApiResponse<ShopInfoResponse>> getShopInfo(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long shopId) {
        
        String token = jwtUtil.getTokenFromHeader(authHeader);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.success("Invalid or missing token", null));
        }

        ShopInfoResponse shopInfo = customerService.getShopInfo(shopId);
        return ResponseEntity.ok(ApiResponse.success("Shop information fetched successfully", shopInfo));
    }
}
