package com.stitcho.beta.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stitcho.beta.dto.ApiResponse;
import com.stitcho.beta.service.TaskService;
import com.stitcho.beta.util.JwtUtil;

import lombok.RequiredArgsConstructor;

/**
 * 🔐 SECURE TASK CONTROLLER
 * Worker can only start/complete their own tasks
 * workerId extracted from JWT automatically
 */
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;
    private final JwtUtil jwtUtil;

    @PutMapping("/{taskId}/start")
    public ResponseEntity<ApiResponse<Void>> startTask(
            @PathVariable Long taskId,
            @RequestHeader("Authorization") String authHeader) {
        
        String token = jwtUtil.getTokenFromHeader(authHeader);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.success("Invalid or missing token", null));
        }

        Long userId = jwtUtil.extractUserId(token);
        taskService.startTask(taskId, userId);
        return ResponseEntity.ok(ApiResponse.success("Task started"));
    }

    @PutMapping("/{taskId}/complete")
    public ResponseEntity<ApiResponse<Void>> completeTask(
            @PathVariable Long taskId,
            @RequestHeader("Authorization") String authHeader) {
        
        String token = jwtUtil.getTokenFromHeader(authHeader);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.success("Invalid or missing token", null));
        }

        Long userId = jwtUtil.extractUserId(token);
        taskService.completeTask(taskId, userId);
        return ResponseEntity.ok(ApiResponse.success("Task completed successfully"));
    }
}
