package com.stitcho.beta.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkerTaskResponse {
    private Long taskId;
    private String taskType;
    private String status;
    private LocalDateTime assignedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private OrderInfo order;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderInfo {
        private Long orderId;
        private String customerName;
        private LocalDate deadline;
        private String orderStatus;
    }
}
