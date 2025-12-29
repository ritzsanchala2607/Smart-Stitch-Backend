package com.stitcho.beta.dto;

import java.util.Map;

import com.stitcho.beta.entity.DressType;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MeasurementProfileRequest {
    
    @NotNull(message = "Customer ID is required")
    private Long customerId;
    
    @NotNull(message = "Dress type is required")
    private DressType dressType;
    
    private String notes;
    
    @NotNull(message = "Measurements are required")
    private Map<String, Double> measurements;  // key -> value pairs
}
