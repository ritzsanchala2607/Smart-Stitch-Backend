package com.stitcho.beta.dto;

import java.time.LocalDateTime;

import com.stitcho.beta.entity.ActivityType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecentActivityResponse {
    private Long activityId;
    private Long orderId;
    private ActivityType activityType;
    private String description;
    private String oldStatus;
    private String newStatus;
    private LocalDateTime timestamp;
    
    // Additional order context
    private String orderItemName;
    private Integer daysUntilDeadline;
}
