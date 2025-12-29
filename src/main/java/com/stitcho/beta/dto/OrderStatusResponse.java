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
public class OrderStatusResponse {
    private Long orderId;
    private String orderStatus;
    private LocalDate deadline;
    private String customerName;
    private LocalDateTime createdAt;
    private List<String> taskStatuses; // e.g., ["CUTTING_IN_PROGRESS", "STITCHING_PENDING"]

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskStatusInfo {
        private String taskType;
        private String status;
        private String combined; // e.g., "CUTTING_IN_PROGRESS"
    }
}
