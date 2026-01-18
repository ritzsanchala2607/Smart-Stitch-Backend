package com.stitcho.beta.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String message;
    private Long userId;
    private String email;
    private String name;
    private String role;
    private String jwt;
    private Long shopId;
    private Long customerId;
    private Long workerId;
}
