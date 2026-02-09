package com.stitcho.beta.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.stitcho.beta.dto.ApiResponse;
import com.stitcho.beta.dto.BillResponse;
import com.stitcho.beta.dto.OrderPaymentResponse;
import com.stitcho.beta.dto.UpdatePaymentRequest;
import com.stitcho.beta.service.PaymentService;
import com.stitcho.beta.util.JwtUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 💰 PAYMENT CONTROLLER
 * Handles payment updates, payment history, and bill generation
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {
    RequestMethod.GET, RequestMethod.PUT, RequestMethod.OPTIONS
})
public class PaymentController {
    private final PaymentService paymentService;
    private final JwtUtil jwtUtil;

    /**
     * Update payment for an order
     * PUT /api/orders/{orderId}/payment
     */
    @PutMapping("/{orderId}/payment")
    public ResponseEntity<ApiResponse<OrderPaymentResponse>> updatePayment(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long orderId,
            @Valid @RequestBody UpdatePaymentRequest request) {
        
        String token = jwtUtil.getTokenFromHeader(authHeader);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.success("Invalid or missing token", null));
        }

        Long userId = jwtUtil.extractUserId(token);
        String role = jwtUtil.extractRole(token);
        
        if (!"OWNER".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.success("Only owners can update payments", null));
        }

        try {
            OrderPaymentResponse response = paymentService.updatePayment(userId, orderId, request);
            return ResponseEntity.ok(ApiResponse.success("Payment updated successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.success(e.getMessage(), null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.success(e.getMessage(), null));
        }
    }

    /**
     * Get payment history for an order
     * GET /api/orders/{orderId}/payments
     */
    @GetMapping("/{orderId}/payments")
    public ResponseEntity<ApiResponse<OrderPaymentResponse>> getPaymentHistory(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long orderId) {
        
        String token = jwtUtil.getTokenFromHeader(authHeader);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.success("Invalid or missing token", null));
        }

        String role = jwtUtil.extractRole(token);
        
        if (!"OWNER".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.success("Only owners can view payment history", null));
        }

        try {
            OrderPaymentResponse response = paymentService.getOrderPaymentInfo(orderId);
            return ResponseEntity.ok(ApiResponse.success("Payment history fetched successfully", response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.success(e.getMessage(), null));
        }
    }

    /**
     * Generate bill for an order
     * GET /api/orders/{orderId}/bill
     */
    @GetMapping("/{orderId}/bill")
    public ResponseEntity<ApiResponse<BillResponse>> generateBill(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long orderId) {
        
        String token = jwtUtil.getTokenFromHeader(authHeader);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.success("Invalid or missing token", null));
        }

        Long userId = jwtUtil.extractUserId(token);
        String role = jwtUtil.extractRole(token);
        
        if (!"OWNER".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.success("Only owners can generate bills", null));
        }

        try {
            BillResponse bill = paymentService.generateBill(userId, orderId);
            return ResponseEntity.ok(ApiResponse.success("Bill generated successfully", bill));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.success(e.getMessage(), null));
        }
    }
}
