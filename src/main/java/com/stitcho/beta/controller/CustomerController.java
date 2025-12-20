package com.stitcho.beta.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stitcho.beta.dto.ApiResponse;
import com.stitcho.beta.dto.CreateCustomerRequest;
import com.stitcho.beta.dto.CreateCustomerResponse;
import com.stitcho.beta.dto.CustomerResponse;
import com.stitcho.beta.dto.UpdateCustomerRequest;
import com.stitcho.beta.service.CustomerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;

    @PostMapping("/{shopId}/customers")
    public ResponseEntity<ApiResponse<CreateCustomerResponse>> createCustomer(
            @PathVariable Long shopId,
            @Valid @RequestBody CreateCustomerRequest request) {
        CreateCustomerResponse response = customerService.createCustomer(shopId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Customer created successfully", response));
    }

    @GetMapping("/{shopId}/customers/{customerId}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomer(
            @PathVariable Long shopId,
            @PathVariable Long customerId) {
        CustomerResponse response = customerService.getCustomer(shopId, customerId);
        return ResponseEntity.ok(ApiResponse.success("Customer fetched successfully", response));
    }

    @PutMapping("/{shopId}/customers/{customerId}")
    public ResponseEntity<ApiResponse<Void>> updateCustomer(
            @PathVariable Long shopId,
            @PathVariable Long customerId,
            @RequestBody UpdateCustomerRequest request) {
        customerService.updateCustomer(shopId, customerId, request);
        return ResponseEntity.ok(ApiResponse.success("Customer updated successfully"));
    }
}
