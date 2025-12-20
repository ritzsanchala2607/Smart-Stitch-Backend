package com.stitcho.beta.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
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
public class CreateOrderRequest {
    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Deadline is required")
    private LocalDate deadline;

    @NotNull(message = "Total price is required")
    @Positive(message = "Total price must be positive")
    private Double totalPrice;

    @NotNull(message = "Advance payment is required")
    @Positive(message = "Advance payment must be positive")
    private Double advancePayment;

    private String additionalNotes;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<OrderItem> items;

    @NotEmpty(message = "At least one task is required")
    @Valid
    private List<TaskRequest> tasks;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        @NotNull(message = "Item name is required")
        private String itemName;

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        private Integer quantity;

        @NotNull(message = "Price is required")
        @Positive(message = "Price must be positive")
        private Double price;

        private String fabricType;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskRequest {
        @NotNull(message = "Worker ID is required")
        private Long workerId;

        @NotNull(message = "Task type is required")
        private String taskType;

        // Optional: for item-specific assignment
        private Long orderItemId;
    }
}
