package com.stitcho.beta.service;

import org.springframework.stereotype.Service;

import com.stitcho.beta.Repository.RoleRepository;
import com.stitcho.beta.Repository.UserRepository;
import com.stitcho.beta.dto.AuthResponse;
import com.stitcho.beta.entity.Role;
import com.stitcho.beta.entity.User;
import com.stitcho.beta.util.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OAuth2Service {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtUtil jwtUtil;

    public AuthResponse handleGoogleLogin(String email, String name, String googleId, String picture) {
        User user = userRepository.findByEmail(email);

        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setGoogleId(googleId);
            user.setProfilePicture(picture);
            user.setProvider("google");
            user.setPassword("");

            Role customerRole = roleRepository.findByRoleName("customer");
            if (customerRole == null) {
                throw new IllegalArgumentException("Customer role not found. Please contact administrator.");
            }
            user.setRole(customerRole);
            user = userRepository.save(user);
        } else {
            user.setGoogleId(googleId);
            user.setProfilePicture(picture);
            user.setProvider("google");
            user = userRepository.save(user);
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getName(), user.getRole().getRoleName());

        AuthResponse response = new AuthResponse();
        response.setMessage("OAuth2 login successful");
        response.setEmail(user.getEmail());
        response.setName(user.getName());
        response.setRole(user.getRole().getRoleName());
        response.setJwt(token);

        return response;
    }
}
