package com.stitcho.beta.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOwnerShopRequest {
    // User fields
    private String name;
    private String contactNumber;
    private String profilePicture;
    
    // Shop fields
    private String shopName;
    
    @Email(message = "Shop email should be valid")
    private String shopEmail;
    
    private String shopContactNumber;
    private String shopAddress;
}
