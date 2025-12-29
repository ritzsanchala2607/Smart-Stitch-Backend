package com.stitcho.beta.dto;

import java.time.LocalDateTime;
import java.util.Map;

import com.stitcho.beta.entity.DressType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MeasurementProfileResponse {
    
    private Long profileId;
    private Long customerId;
    private String customerName;
    private DressType dressType;
    private String notes;
    private Map<String, Double> measurements;  // key -> value pairs
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
