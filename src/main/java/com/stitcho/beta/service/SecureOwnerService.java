package com.stitcho.beta.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stitcho.beta.Repository.OwnerRepository;
import com.stitcho.beta.Repository.ShopRepository;
import com.stitcho.beta.Repository.UserRepository;
import com.stitcho.beta.dto.OwnerProfileResponse;
import com.stitcho.beta.dto.UpdateOwnerProfileRequest;
import com.stitcho.beta.entity.Owner;
import com.stitcho.beta.entity.Shop;
import com.stitcho.beta.entity.User;

import lombok.RequiredArgsConstructor;

/**
 * 🔐 SECURE OWNER SERVICE
 * Owner can view/update their own shop profile
 * shopId extracted from JWT (no manual passing)
 */
@Service
@RequiredArgsConstructor
public class SecureOwnerService {
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final OwnerRepository ownerRepository;

    public OwnerProfileResponse getMyShopProfile(Long userId) {
        Owner owner = ownerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        User user = owner.getUser();
        Shop shop = owner.getShop();

        OwnerProfileResponse response = new OwnerProfileResponse();
        response.setUserId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setContactNumber(user.getContactNumber());
        response.setProfilePicture(user.getProfilePicture());

        response.setShopId(shop.getShopId());
        response.setShopName(shop.getShopName());
        response.setShopEmail(shop.getShopEmail());
        response.setShopContactNumber(shop.getShopMobileNo());
        response.setShopAddress(shop.getShopAddress());

        return response;
    }

    @Transactional
    public OwnerProfileResponse updateMyShopProfile(Long userId, UpdateOwnerProfileRequest request) {
        Owner owner = ownerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        User user = owner.getUser();
        Shop shop = owner.getShop();

        // Update user details
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getContactNumber() != null) {
            user.setContactNumber(request.getContactNumber());
        }
        if (request.getProfilePicture() != null) {
            user.setProfilePicture(request.getProfilePicture());
        }
        userRepository.save(user);

        // Update shop details
        if (request.getShopName() != null) {
            shop.setShopName(request.getShopName());
        }
        if (request.getShopEmail() != null) {
            shop.setShopEmail(request.getShopEmail());
        }
        if (request.getShopContactNumber() != null) {
            shop.setShopMobileNo(request.getShopContactNumber());
        }
        if (request.getShopAddress() != null) {
            shop.setShopAddress(request.getShopAddress());
        }
        shopRepository.save(shop);

        return getMyShopProfile(userId);
    }
}
