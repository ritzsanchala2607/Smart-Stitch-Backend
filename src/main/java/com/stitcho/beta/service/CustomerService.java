package com.stitcho.beta.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stitcho.beta.Repository.CustomerRepository;
import com.stitcho.beta.Repository.RoleRepository;
import com.stitcho.beta.Repository.ShopRepository;
import com.stitcho.beta.Repository.UserRepository;
import com.stitcho.beta.dto.CreateCustomerRequest;
import com.stitcho.beta.dto.CreateCustomerResponse;
import com.stitcho.beta.dto.CustomerResponse;
import com.stitcho.beta.dto.UpdateCustomerRequest;
import com.stitcho.beta.entity.Customer;
import com.stitcho.beta.entity.Role;
import com.stitcho.beta.entity.Shop;
import com.stitcho.beta.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final ShopRepository shopRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public CreateCustomerResponse createCustomer(Long shopId, CreateCustomerRequest request) {
        // 1️⃣ Validate shop
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        // 2️⃣ Check if email already exists
        if (userRepository.findByEmail(request.getUser().getEmail()) != null) {
            throw new IllegalArgumentException("Email already in use");
        }

        // 3️⃣ Create user with CUSTOMER role
        Role customerRole = roleRepository.findById(request.getUser().getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        User user = new User();
        user.setName(request.getUser().getName());
        user.setEmail(request.getUser().getEmail());
        user.setContactNumber(request.getUser().getContactNumber());
        user.setPassword(passwordEncoder.encode(request.getUser().getPassword()));
        user.setProfilePicture(request.getUser().getProfilePicture());
        user.setRole(customerRole);
        user = userRepository.save(user);

        // 4️⃣ Create customer
        Customer customer = new Customer();
        customer.setUser(user);
        customer.setShop(shop);
        customer = customerRepository.save(customer);

        // Note: Measurements are now managed through the Measurement API
        // Use POST /api/measurements to create measurement profiles

        return new CreateCustomerResponse(customer.getId(), user.getId());
    }

    public CustomerResponse getCustomer(Long shopId, Long customerId) {
        Customer customer = customerRepository.findByIdAndShop_ShopId(customerId, shopId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        CustomerResponse response = new CustomerResponse();
        response.setCustomerId(customer.getId());
        response.setCreatedAt(customer.getCreatedAt());

        // User info
        User user = customer.getUser();
        CustomerResponse.UserInfo userInfo = new CustomerResponse.UserInfo();
        userInfo.setName(user.getName());
        userInfo.setEmail(user.getEmail());
        userInfo.setContactNumber(user.getContactNumber());
        userInfo.setProfilePicture(user.getProfilePicture());
        response.setUser(userInfo);

        // Note: Measurements are now retrieved through the Measurement API
        // Use GET /api/measurements/customer/{customerId}

        return response;
    }

    @Transactional
    public void updateCustomer(Long shopId, Long customerId, UpdateCustomerRequest request) {
        Customer customer = customerRepository.findByIdAndShop_ShopId(customerId, shopId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Update user info
        if (request.getUser() != null) {
            User user = customer.getUser();
            if (request.getUser().getName() != null) {
                user.setName(request.getUser().getName());
            }
            if (request.getUser().getContactNumber() != null) {
                user.setContactNumber(request.getUser().getContactNumber());
            }
            if (request.getUser().getProfilePicture() != null) {
                user.setProfilePicture(request.getUser().getProfilePicture());
            }
            userRepository.save(user);
        }

        // Note: Measurements are now managed through the Measurement API
        // Use PUT /api/measurements/{profileId} to update measurements
    }
}
