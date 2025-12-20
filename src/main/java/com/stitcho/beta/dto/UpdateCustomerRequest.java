package com.stitcho.beta.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCustomerRequest {
    private UpdateUserDto user;
    private MeasurementDto measurements;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateUserDto {
        private String name;
        private String contactNumber;
        private String profilePicture;
    }
}
