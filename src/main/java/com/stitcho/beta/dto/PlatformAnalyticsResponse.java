package com.stitcho.beta.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlatformAnalyticsResponse {
    private SystemMetrics systemMetrics;
    private List<MonthlyData> ordersVsShopsGrowth;
    private List<MonthlyActiveUsers> monthlyActiveUsers;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SystemMetrics {
        private Long ordersToday;
        private Long ordersThisWeek;
        private Long ordersThisMonth;
        private Double averageOrdersPerShop;
        private Double averageWorkersPerShop;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyData {
        private String month;
        private Long orders;
        private Long shops;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyActiveUsers {
        private String month;
        private Long owners;
        private Long workers;
    }
}
