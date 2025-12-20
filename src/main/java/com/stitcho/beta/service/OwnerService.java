package com.stitcho.beta.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stitcho.beta.Repository.OwnerRepository;
import com.stitcho.beta.Repository.RoleRepository;
import com.stitcho.beta.Repository.ShopRepository;
import com.stitcho.beta.Repository.UserRepository;
import com.stitcho.beta.dto.CreateOwnerRequest;
import com.stitcho.beta.dto.OwnerProfileResponse;
import com.stitcho.beta.dto.UpdateOwnerProfileRequest;
import com.stitcho.beta.entity.Owner;
import com.stitcho.beta.entity.Role;
import com.stitcho.beta.entity.Shop;
import com.stitcho.beta.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OwnerService {
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final OwnerRepository ownerRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void createOwnerWithShop(CreateOwnerRequest request) {
        // 1️⃣ Check if email already exists
        if (userRepository.findByEmail(request.getUser().getEmail()) != null) {
            throw new IllegalArgumentException("Email already in use");
        }

        // 2️⃣ Create User
        Role role = roleRepository.findById(request.getUser().getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        User user = new User();
        user.setName(request.getUser().getName());
        user.setEmail(request.getUser().getEmail());
        user.setPassword(passwordEncoder.encode(request.getUser().getPassword()));
        user.setContactNumber(request.getUser().getContactNumber());
        user.setRole(role);
        user = userRepository.save(user);

        // 3️⃣ Create Shop
        Shop shop = new Shop();
        shop.setShopName(request.getShop().getName());
        shop.setShopEmail(request.getShop().getEmail());
        shop.setShopMobileNo(request.getShop().getContactNumber());
        shop.setShopAddress(request.getShop().getAddress());
        shop = shopRepository.save(shop);

        // 4️⃣ Create Owner Mapping
        Owner owner = new Owner();
        owner.setUser(user);
        owner.setShop(shop);
        ownerRepository.save(owner);
    }

    public OwnerProfileResponse getOwnerProfile(Long userId) {
        Owner owner = ownerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Owner not found for user ID: " + userId));

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
    public OwnerProfileResponse updateOwnerProfile(Long userId, UpdateOwnerProfileRequest request) {
        Owner owner = ownerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Owner not found for user ID: " + userId));

        User user = owner.getUser();
        Shop shop = owner.getShop();

        // Update user details
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setContactNumber(request.getContactNumber());
        user.setProfilePicture(request.getProfilePicture());
        userRepository.save(user);

        // Update shop details
        shop.setShopName(request.getShopName());
        shop.setShopEmail(request.getShopEmail());
        shop.setShopMobileNo(request.getShopContactNumber());
        shop.setShopAddress(request.getShopAddress());
        shopRepository.save(shop);

        return getOwnerProfile(userId);
    }
}
