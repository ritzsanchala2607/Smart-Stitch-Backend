package com.stitcho.beta.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long paymentId;
    private Double amount;
    private String paymentMethod;
    private LocalDateTime paymentDate;
    private String note;
    private String recordedBy;
    private LocalDateTime createdAt;
}
