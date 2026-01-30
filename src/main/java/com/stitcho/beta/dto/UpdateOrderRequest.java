package com.stitcho.beta.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderRequest {
    private LocalDate deadline;
    private Double totalPrice;
    private Double paidAmount;
    private String paymentStatus;
    private String notes;
    private String status;  // Order status: NEW, CUTTING, STITCHING, IRONING, COMPLETED, DELIVERED, CANCELLED
}
