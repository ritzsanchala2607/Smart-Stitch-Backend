package com.stitcho.beta.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderPaymentResponse {
    private Long orderId;
    private Double totalPrice;
    private Double paidAmount;
    private Double balanceAmount;
    private String paymentStatus; // PAID, PARTIAL, PENDING
    private List<PaymentResponse> paymentHistory;
}
