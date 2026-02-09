package com.stitcho.beta.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePaymentRequest {
    @NotNull(message = "Additional payment amount is required")
    @Positive(message = "Payment amount must be positive")
    private Double additionalPayment;

    @NotNull(message = "Payment method is required")
    private String paymentMethod; // CASH, CARD, UPI, BANK_TRANSFER, OTHER

    private String paymentNote;

    private LocalDateTime paymentDate; // If null, use current time
}
