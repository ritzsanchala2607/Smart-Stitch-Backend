package com.stitcho.beta.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.stitcho.beta.Repository.RoleRepository;
import com.stitcho.beta.Repository.UserRepository;
import com.stitcho.beta.dto.AuthResponse;
import com.stitcho.beta.dto.LoginRequest;
import com.stitcho.beta.dto.RegisterRequest;
import com.stitcho.beta.entity.Role;
import com.stitcho.beta.entity.User;
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

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // 1. check existing user
        if (userRepository.findByEmail(request.getEmail()) != null) {
            throw new IllegalArgumentException("Email already in use.");
        }

        // 2. fetch or create role
        Role role = roleRepository.findByRoleName(request.getRole());
        if (role == null) {
            role = new Role();
            role.setRoleName(request.getRole());
            role = roleRepository.save(role);
        }

        // 3. create user and encode password
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);

        user = userRepository.save(user);

        // 4. prepare response
        String token = jwtUtil.generateToken(user.getEmail(), user.getName(), role.getRoleName());
        AuthResponse resp = new AuthResponse();
        resp.setMessage("User registered successfully");
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

        // Validate role matches
        if (user.getRole() == null || !user.getRole().getRoleName().equals(request.getRole())) {
            throw new IllegalArgumentException("Invalid role for this user.");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getName(), user.getRole().getRoleName());
        AuthResponse resp = new AuthResponse();
        resp.setMessage("Login successful");
        resp.setEmail(user.getEmail());
        resp.setName(user.getName());
        resp.setRole(user.getRole().getRoleName());
        resp.setJwt(token);
        return resp;
    }
}
