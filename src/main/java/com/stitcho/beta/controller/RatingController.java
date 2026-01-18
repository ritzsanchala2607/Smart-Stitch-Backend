package com.stitcho.beta.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stitcho.beta.dto.OrderForRatingResponse;
import com.stitcho.beta.dto.RatingResponse;
import com.stitcho.beta.dto.RatingSummary;
import com.stitcho.beta.dto.ShopRatingRequest;
import com.stitcho.beta.dto.WorkerRatingRequest;
import com.stitcho.beta.util.JwtUtil;
import com.stitcho.beta.service.RatingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    @Autowired
    private RatingService ratingService;

    @Autowired
    private JwtUtil jwtUtil;

    // ==================== SHOP RATING ENDPOINTS ====================

    /**
     * Customer rates a shop after completing an order
     * POST /api/ratings/shop
     */
    @PostMapping("/shop")
    public ResponseEntity<Map<String, Object>> rateShop(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ShopRatingRequest request) {
        
        try {
            // Extract and validate JWT token
            String token = jwtUtil.getTokenFromHeader(authHeader);
            if (token == null || !jwtUtil.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Invalid or missing token"));
            }

            // Extract customer ID from JWT
            Long customerId = jwtUtil.extractCustomerId(token);
            if (customerId == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("Only customers can rate shops"));
            }

            RatingResponse rating = ratingService.rateShop(customerId, request);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Shop rated successfully");
            response.put("data", rating);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * Get all ratings for a shop
     * GET /api/ratings/shop/{shopId}
     */
    @GetMapping("/shop/{shopId}")
    public ResponseEntity<Map<String, Object>> getShopRatings(@PathVariable Long shopId) {
        try {
            List<RatingResponse> ratings = ratingService.getShopRatings(shopId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Shop ratings fetched successfully");
            response.put("data", ratings);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * Get shop rating summary (average and count)
     * GET /api/ratings/shop/{shopId}/summary
     */
    @GetMapping("/shop/{shopId}/summary")
    public ResponseEntity<Map<String, Object>> getShopRatingSummary(@PathVariable Long shopId) {
        try {
            RatingSummary summary = ratingService.getShopRatingSummary(shopId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Shop rating summary fetched successfully");
            response.put("data", summary);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    // ==================== WORKER RATING ENDPOINTS ====================

    /**
     * Customer rates a worker after completing an order
     * POST /api/ratings/worker
     */
    @PostMapping("/worker")
    public ResponseEntity<Map<String, Object>> rateWorker(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody WorkerRatingRequest request) {
        
        try {
            // Extract and validate JWT token
            String token = jwtUtil.getTokenFromHeader(authHeader);
            if (token == null || !jwtUtil.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Invalid or missing token"));
            }

            // Extract customer ID from JWT
            Long customerId = jwtUtil.extractCustomerId(token);
            if (customerId == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("Only customers can rate workers"));
            }

            RatingResponse rating = ratingService.rateWorker(customerId, request);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Worker rated successfully");
            response.put("data", rating);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * Get all ratings for a worker
     * GET /api/ratings/worker/{workerId}
     */
    @GetMapping("/worker/{workerId}")
    public ResponseEntity<Map<String, Object>> getWorkerRatings(@PathVariable Long workerId) {
        try {
            List<RatingResponse> ratings = ratingService.getWorkerRatings(workerId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Worker ratings fetched successfully");
            response.put("data", ratings);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * Get worker rating summary (average and count)
     * GET /api/ratings/worker/{workerId}/summary
     */
    @GetMapping("/worker/{workerId}/summary")
    public ResponseEntity<Map<String, Object>> getWorkerRatingSummary(@PathVariable Long workerId) {
        try {
            RatingSummary summary = ratingService.getWorkerRatingSummary(workerId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Worker rating summary fetched successfully");
            response.put("data", summary);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    // ==================== HELPER METHODS ====================

    /**
     * Get customer orders with worker details for rating
     * GET /api/ratings/my-orders
     */
    @GetMapping("/my-orders")
    public ResponseEntity<Map<String, Object>> getMyOrdersForRating(
            @RequestHeader("Authorization") String authHeader) {
        try {
            // Extract and validate JWT token
            String token = jwtUtil.getTokenFromHeader(authHeader);
            if (token == null || !jwtUtil.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Invalid or missing token"));
            }

            // Extract customer ID from JWT
            Long customerId = jwtUtil.extractCustomerId(token);
            if (customerId == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("Only customers can access this endpoint"));
            }

            List<OrderForRatingResponse> orders = ratingService.getCustomerOrdersForRating(customerId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Orders fetched successfully");
            response.put("data", orders);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    // ==================== HELPER METHODS ====================

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }
}
