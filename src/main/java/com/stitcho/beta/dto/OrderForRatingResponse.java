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
public class OrderForRatingResponse {
    private Long orderId;
    private String shopName;
    private LocalDate deadline;
    private String status;
    private LocalDateTime createdAt;
    private List<WorkerTaskInfo> workers;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkerTaskInfo {
        private Long workerId;
        private String workerName;
        private String taskType;
        private String taskStatus;
        private Boolean alreadyRated;
    }
}
