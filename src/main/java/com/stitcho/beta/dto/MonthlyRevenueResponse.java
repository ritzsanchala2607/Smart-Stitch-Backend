package com.stitcho.beta.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyRevenueResponse {
    private Integer year;
    private List<MonthRevenue> monthlyData;
    private Double totalYearRevenue;
    private Double averageMonthlyRevenue;
    private String highestRevenueMonth;
    private Double highestRevenueAmount;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthRevenue {
        private Integer month;
        private String monthName;
        private Double revenue;
        private Integer completedOrders;
        private Double averageOrderValue;
    }
}
