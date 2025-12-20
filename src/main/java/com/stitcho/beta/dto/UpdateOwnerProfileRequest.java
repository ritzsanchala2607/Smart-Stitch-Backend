package com.stitcho.beta.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOwnerProfileRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Contact number is required")
    private String contactNumber;

    private String profilePicture;

    @NotBlank(message = "Shop name is required")
    private String shopName;

    @NotBlank(message = "Shop email is required")
    @Email(message = "Shop email should be valid")
    private String shopEmail;

    @NotBlank(message = "Shop contact number is required")
    private String shopContactNumber;

    @NotBlank(message = "Shop address is required")
    private String shopAddress;
}
