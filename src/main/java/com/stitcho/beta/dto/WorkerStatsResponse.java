package com.stitcho.beta.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkerStatsResponse {
    private Integer totalTasks;
    private Integer pendingTasks;
    private Integer inProgressTasks;
    private Integer completedTasks;
    private Double averageRating;
    private Integer totalRatings;
}
