package com.stitcho.beta.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stitcho.beta.dto.ApiResponse;
import com.stitcho.beta.dto.CreateWorkerRequest;
import com.stitcho.beta.dto.WorkerResponse;
import com.stitcho.beta.entity.Task;
import com.stitcho.beta.service.SecureWorkerService;
import com.stitcho.beta.util.JwtUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 🔐 SECURE WORKER CONTROLLER
 * Owner can manage workers in their shop
 * Worker can view their own tasks
 */
@RestController
@RequestMapping("/api/workers")
@RequiredArgsConstructor
public class SecureWorkerController {
    private final SecureWorkerService workerService;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<ApiResponse<String>> addWorker(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody CreateWorkerRequest request) {
        
        String token = jwtUtil.getTokenFromHeader(authHeader);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.success("Invalid or missing token", null));
        }

        Long userId = jwtUtil.extractUserId(token);
        String role = jwtUtil.extractRole(token);
        
        if (!"OWNER".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.success("Only owners can add workers", null));
        }

        workerService.createWorker(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Worker created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WorkerResponse>>> getWorkers(
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
                    .body(ApiResponse.success("Only owners can view workers", null));
        }

        List<WorkerResponse> workers = workerService.getMyShopWorkers(userId, name);
        return ResponseEntity.ok(ApiResponse.success("Workers fetched successfully", workers));
    }

    @GetMapping("/me/tasks")
    public ResponseEntity<ApiResponse<List<Task>>> getMyTasks(
            @RequestHeader("Authorization") String authHeader) {
        
        String token = jwtUtil.getTokenFromHeader(authHeader);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.success("Invalid or missing token", null));
        }

        Long userId = jwtUtil.extractUserId(token);
        String role = jwtUtil.extractRole(token);
        
        if (!"WORKER".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.success("Only workers can view tasks", null));
        }

        List<Task> tasks = workerService.getMyTasks(userId);
        return ResponseEntity.ok(ApiResponse.success("Tasks fetched successfully", tasks));
    }
}
