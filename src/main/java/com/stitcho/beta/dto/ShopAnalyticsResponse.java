package com.stitcho.beta.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShopAnalyticsResponse {
    private OverviewStats overview;
    private List<DailyOrderTrend> dailyOrderTrend;
    private List<MonthlyRevenueTrend> monthlyRevenueTrend;
    private OrderStatusDistribution orderStatusDistribution;
    private List<WorkerPerformance> workerPerformance;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverviewStats {
        private Long totalOrders;
        private Long activeOrders;
        private Long completedOrders;
        private Double totalRevenue;
        private Double pendingPayments;
        private Integer totalCustomers;
        private Integer totalWorkers;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyOrderTrend {
        private String day;
        private Integer orderCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyRevenueTrend {
        private String month;
        private Double revenue;
        private Double expense;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderStatusDistribution {
        private Integer pending;
        private Integer cutting;
        private Integer stitching;
        private Integer fitting;
        private Integer ready;
        private Integer completed;
        private Integer total;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkerPerformance {
        private Long workerId;
        private String workerName;
        private String specialty;
        private Integer totalTasks;
        private Integer completedTasks;
        private Double performancePercentage;
        private Double averageRating;
        private Double completionRate;
    }
}
