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

import com.stitcho.beta.dto.CreateOwnerRequest;
import com.stitcho.beta.dto.OwnerProfileResponse;
import com.stitcho.beta.dto.UpdateOwnerProfileRequest;
import com.stitcho.beta.service.OwnerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/owners")
@RequiredArgsConstructor
public class OwnerController {
    private final OwnerService ownerService;

    @PostMapping("/create")
    public ResponseEntity<String> createOwnerWithShop(@Valid @RequestBody CreateOwnerRequest request) {
        ownerService.createOwnerWithShop(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("Owner and shop created successfully");
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<OwnerProfileResponse> getOwnerProfile(@PathVariable Long userId) {
        OwnerProfileResponse profile = ownerService.getOwnerProfile(userId);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile/{userId}")
    public ResponseEntity<OwnerProfileResponse> updateOwnerProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateOwnerProfileRequest request) {
        OwnerProfileResponse updatedProfile = ownerService.updateOwnerProfile(userId, request);
        return ResponseEntity.ok(updatedProfile);
    }
}
