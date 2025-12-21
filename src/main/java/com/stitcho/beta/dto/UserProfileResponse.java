package com.stitcho.beta.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private Long userId;
    private String name;
    private String email;
    private String contactNumber;
    private String profilePicture;
    private String role;
    
    // Role-specific IDs (populated based on role)
    private Long shopId;      // For OWNER
    private Long customerId;  // For CUSTOMER
    private Long workerId;    // For WORKER
}
