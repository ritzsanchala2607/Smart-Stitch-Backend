package com.stitcho.beta.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stitcho.beta.dto.ApiResponse;
import com.stitcho.beta.dto.CreateOrderRequest;
import com.stitcho.beta.dto.OrderResponse;
import com.stitcho.beta.service.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/{shopId}/orders")
    public ResponseEntity<ApiResponse<Long>> createOrder(
            @PathVariable Long shopId,
            @Valid @RequestBody CreateOrderRequest request) {
        Long orderId = orderService.createOrder(shopId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order created and tasks assigned", orderId));
    }

    @GetMapping("/{shopId}/orders/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
            @PathVariable Long shopId,
            @PathVariable Long orderId) {
        OrderResponse response = orderService.getOrder(shopId, orderId);
        return ResponseEntity.ok(ApiResponse.success("Order fetched successfully", response));
    }

    @GetMapping("/{shopId}/customers/{customerId}/orders")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getCustomerOrders(
            @PathVariable Long shopId,
            @PathVariable Long customerId) {
        List<OrderResponse> orders = orderService.getCustomerOrders(shopId, customerId);
        return ResponseEntity.ok(ApiResponse.success("Orders fetched successfully", orders));
    }

    @GetMapping("/{shopId}/orders")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getShopOrders(@PathVariable Long shopId) {
        List<OrderResponse> orders = orderService.getShopOrders(shopId);
        return ResponseEntity.ok(ApiResponse.success("Orders fetched successfully", orders));
    }
}
