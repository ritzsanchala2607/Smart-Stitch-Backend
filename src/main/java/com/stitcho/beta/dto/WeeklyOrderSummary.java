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
public class WeeklyOrderSummary {
    private Integer totalOrders;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<OrderSummary> orders;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderSummary {
        private Long orderId;
        private String customerName;
        private List<String> items;
        private Double totalAmount;
        private List<WorkerInfo> workers;
        private String status;
        private LocalDate deliveryDate;
        private LocalDateTime createdAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkerInfo {
        private Long workerId;
        private String workerName;
        private String taskType;
        private String taskStatus;
    }
}
