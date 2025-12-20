package com.stitcho.beta.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OwnerProfileResponse {
    private Long userId;
    private String name;
    private String email;
    private String contactNumber;
    private String profilePicture;
    
    private Long shopId;
    private String shopName;
    private String shopEmail;
    private String shopContactNumber;
    private String shopAddress;
}
