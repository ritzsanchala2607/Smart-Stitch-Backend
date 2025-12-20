package com.stitcho.beta.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stitcho.beta.dto.ApiResponse;
import com.stitcho.beta.service.TaskService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/workers/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @PutMapping("/{taskId}/start")
    public ResponseEntity<ApiResponse<Void>> startTask(
            @PathVariable Long taskId,
            @RequestParam Long workerId) {
        taskService.startTask(taskId, workerId);
        return ResponseEntity.ok(ApiResponse.success("Task started"));
    }

    @PutMapping("/{taskId}/complete")
    public ResponseEntity<ApiResponse<Void>> completeTask(
            @PathVariable Long taskId,
            @RequestParam Long workerId) {
        taskService.completeTask(taskId, workerId);
        return ResponseEntity.ok(ApiResponse.success("Task completed successfully"));
    }
}
