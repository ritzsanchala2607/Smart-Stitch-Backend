package com.stitcho.beta.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentHistoryResponse {
    private Long orderId;
    private Double totalPrice;
    private Double paidAmount;
    private String paymentStatus;
    private LocalDateTime paymentDate;
    private String notes;
}
