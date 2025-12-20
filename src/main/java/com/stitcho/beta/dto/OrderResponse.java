package com.stitcho.beta.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long orderId;
    private String status;
    private LocalDate deadline;
    private Double totalPrice;
    private Double paidAmount;
    private String paymentStatus;
    private String notes;
    private LocalDateTime createdAt;
    private CustomerInfo customer;
    private List<OrderItemInfo> items;
    private List<TaskInfo> tasks;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerInfo {
        private Long customerId;
        private String name;
        private String email;
        private String contactNumber;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemInfo {
        private Long itemId;
        private String itemName;
        private Integer quantity;
        private Double price;
        private String fabricType;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskInfo {
        private Long taskId;
        private String taskType;
        private String status;
        private Long workerId;
        private String workerName;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
    }
}
