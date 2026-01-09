package com.stitcho.beta.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerStatsResponse {
    private Long totalOrders;
    private Long activeOrders;
    private Long completedOrders;
    private Double totalSpent;
    private Double pendingPayment;
}
