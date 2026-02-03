package com.stitcho.beta.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponse {
    private Long totalShops;
    private Long totalOwners;
    private Long totalWorkers;
    private Long totalOrders;
    private Long activeShops;
    private Double systemGrowth; // Percentage growth
}
