package com.stitcho.beta.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stitcho.beta.Repository.CustomerRepository;
import com.stitcho.beta.Repository.OwnerRepository;
import com.stitcho.beta.Repository.UserRepository;
import com.stitcho.beta.Repository.WorkerRepository;
import com.stitcho.beta.dto.UpdateProfileRequest;
import com.stitcho.beta.dto.UserProfileResponse;
import com.stitcho.beta.entity.Customer;
import com.stitcho.beta.entity.Owner;
import com.stitcho.beta.entity.User;
import com.stitcho.beta.entity.Worker;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final UserRepository userRepository;
    private final OwnerRepository ownerRepository;
    private final CustomerRepository customerRepository;
    private final WorkerRepository workerRepository;

    public UserProfileResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfileResponse response = new UserProfileResponse();
        response.setUserId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setContactNumber(user.getContactNumber());
        response.setProfilePicture(user.getProfilePicture());
        response.setRole(user.getRole().getRoleName());

        // Populate role-specific IDs
        String role = user.getRole().getRoleName().toUpperCase();
        
        if ("OWNER".equals(role)) {
            Optional<Owner> owner = ownerRepository.findByUser_Id(userId);
            owner.ifPresent(o -> response.setShopId(o.getShop().getShopId()));
        } else if ("CUSTOMER".equals(role)) {
            // Assuming customer can have multiple shops, get first one
            // You might want to adjust this logic
            response.setCustomerId(userId); // Simplified
        } else if ("WORKER".equals(role)) {
            Optional<Worker> worker = workerRepository.findByUser_Id(userId);
            worker.ifPresent(w -> {
                response.setWorkerId(w.getId());
                response.setShopId(w.getShop().getShopId());
            });
        }

        return response;
    }

    @Transactional
    public UserProfileResponse updateUserProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getContactNumber() != null) {
            user.setContactNumber(request.getContactNumber());
        }
        if (request.getProfilePicture() != null) {
            user.setProfilePicture(request.getProfilePicture());
        }

        userRepository.save(user);
        return getUserProfile(userId);
    }
}
