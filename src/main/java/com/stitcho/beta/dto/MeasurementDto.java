package com.stitcho.beta.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MeasurementDto {
    private Double chest;
    private Double shoulder;
    private Double shirtLength;
    private Double waist;
    private Double pantLength;
    private Double hip;
    private String customMeasurements;
}
