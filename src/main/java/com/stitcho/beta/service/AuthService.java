package com.stitcho.beta.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.stitcho.beta.Repository.CustomerRepository;
import com.stitcho.beta.Repository.OwnerRepository;
import com.stitcho.beta.Repository.RoleRepository;
import com.stitcho.beta.Repository.UserRepository;
import com.stitcho.beta.Repository.WorkerRepository;
import com.stitcho.beta.dto.AuthResponse;
import com.stitcho.beta.dto.LoginRequest;
import com.stitcho.beta.dto.RegisterRequest;
import com.stitcho.beta.entity.Customer;
import com.stitcho.beta.entity.Role;
import com.stitcho.beta.entity.User;
import com.stitcho.beta.entity.Worker;
import com.stitcho.beta.util.JwtUtil;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final CustomerRepository customerRepository;
    private final WorkerRepository workerRepository;
    private final OwnerRepository ownerRepository;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // 1. check existing user
        if (userRepository.findByEmail(request.getEmail()) != null) {
            throw new IllegalArgumentException("Email already in use.");
        }

        // 2. validate role is one of the allowed roles
        String requestedRole = request.getRole();
        if (!requestedRole.equalsIgnoreCase("OWNER") && !requestedRole.equalsIgnoreCase("WORKER") 
            && !requestedRole.equalsIgnoreCase("CUSTOMER") && !requestedRole.equalsIgnoreCase("ADMIN")) {
            throw new IllegalArgumentException("Invalid role. Allowed roles are: owner, worker, customer");
        }

        // 3. fetch role
        Role role = roleRepository.findByRoleName(requestedRole);
        if (role == null) {
            throw new IllegalArgumentException("Role not found. Please contact administrator.");
        }

        // 4. create user and encode password
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setContactNumber(request.getContactNumber());

        user = userRepository.save(user);

        // 5. Generate JWT with userId
        String token = jwtUtil.generateTokenWithIds(
            user.getEmail(), 
            user.getName(), 
            role.getRoleName(),
            user.getId(),
            null, null, null
        );
        
        AuthResponse resp = new AuthResponse();
        resp.setMessage("User registered successfully");
        resp.setUserId(user.getId());
        resp.setEmail(user.getEmail());
        resp.setName(user.getName());
        resp.setRole(role.getRoleName());
        resp.setJwt(token);
        return resp;
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail());
        if (user == null) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        // Validate role matches (case-insensitive)
        if (user.getRole() == null) {
            throw new IllegalArgumentException("User has no role assigned.");
        }
        
        if (!user.getRole().getRoleName().equalsIgnoreCase(request.getRole())) {
            throw new IllegalArgumentException("Invalid role for this user. Expected: " + user.getRole().getRoleName());
        }

        // Get role-specific IDs based on user role
        Long shopId = null;
        Long customerId = null;
        Long workerId = null;

        String roleName = user.getRole().getRoleName();
        
        if ("OWNER".equalsIgnoreCase(roleName)) {
            // Fetch owner's shop ID
            ownerRepository.findByUser_Id(user.getId()).ifPresent(owner -> {
                // Note: shopId is set in the owner entity, not directly accessible here
                // We'll leave it null for now as it's not critical for most operations
            });
        } else if ("CUSTOMER".equalsIgnoreCase(roleName)) {
            // Fetch customer ID
            customerRepository.findByUser_Id(user.getId()).ifPresent(customer -> {
                // customerId is set via lambda, but we need to use a different approach
            });
            Customer customer = customerRepository.findByUser_Id(user.getId()).orElse(null);
            if (customer != null) {
                customerId = customer.getId();
                if (customer.getShop() != null) {
                    shopId = customer.getShop().getShopId();
                }
            }
        } else if ("WORKER".equalsIgnoreCase(roleName)) {
            // Fetch worker ID
            Worker worker = workerRepository.findByUser_Id(user.getId()).orElse(null);
            if (worker != null) {
                workerId = worker.getId();
                if (worker.getShop() != null) {
                    shopId = worker.getShop().getShopId();
                }
            }
        }

        String token = jwtUtil.generateTokenWithIds(
            user.getEmail(), 
            user.getName(), 
            user.getRole().getRoleName(),
            user.getId(),
            shopId,
            customerId,
            workerId
        );
        
        AuthResponse resp = new AuthResponse();
        resp.setMessage("Login successful");
        resp.setUserId(user.getId());
        resp.setEmail(user.getEmail());
        resp.setName(user.getName());
        resp.setRole(user.getRole().getRoleName());
        resp.setJwt(token);
        return resp;
    }

    @Transactional
    public String resetPassword(String email, String newPassword) {
        // Find user by email
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("User with this email does not exist.");
        }

        // Update password with encoded version
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return "Password reset successfully";
    }
}
