package com.stitcho.beta.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stitcho.beta.dto.CreateWorkerRequest;
import com.stitcho.beta.dto.WorkerResponse;
import com.stitcho.beta.service.WorkerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
public class WorkerController {
    private final WorkerService workerService;

    @PostMapping("/{shopId}/workers")
    public ResponseEntity<String> createWorker(
            @PathVariable Long shopId,
            @Valid @RequestBody CreateWorkerRequest request) {
        workerService.createWorker(shopId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body("Worker created successfully");
    }

    @GetMapping("/{shopId}/workers")
    public ResponseEntity<List<WorkerResponse>> getWorkers(
            @PathVariable Long shopId,
            @RequestParam(required = false) String name) {
        List<WorkerResponse> workers = workerService.getWorkersByShop(shopId, name);
        return ResponseEntity.ok(workers);
    }
}
