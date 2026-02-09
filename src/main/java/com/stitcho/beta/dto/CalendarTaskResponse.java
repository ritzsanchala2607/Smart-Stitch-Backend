package com.stitcho.beta.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalendarTaskResponse {
    private LocalDate date;
    private List<TaskDetail> tasks;
    private Integer totalTasks;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskDetail {
        private Long taskId;
        private Long orderId;
        private String taskType;
        private String status;
        private String customerName;
        private String customerPhone;
        private LocalDate deadline;
        private String workerName;
    }
}
