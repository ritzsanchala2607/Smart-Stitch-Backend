package com.stitcho.beta.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateOwnerRequest {
    @NotNull(message = "User details are required")
    @Valid
    private UserDto user;

    @NotNull(message = "Shop details are required")
    @Valid
    private ShopDto shop;
}
