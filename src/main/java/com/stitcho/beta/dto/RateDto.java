package com.stitcho.beta.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RateDto {
    @NotBlank(message = "Work type is required")
    private String workType;

    @NotNull(message = "Rate is required")
    @Positive(message = "Rate must be positive")
    private Double rate;
}
