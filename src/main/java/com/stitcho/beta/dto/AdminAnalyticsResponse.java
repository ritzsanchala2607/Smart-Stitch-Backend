package com.stitcho.beta.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Admin Analytics Response - Platform-wide analytics for admin dashboard
 * Separate from ShopAnalyticsResponse which is for individual shop owners
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminAnalyticsResponse {
    private ShopStatusDistribution shopStatusDistribution;
    private WorkersDistribution workersDistribution;
    private List<MonthlyShopRegistration> monthlyShopRegistrations;
    private List<MonthlyOrdersProcessed> monthlyOrdersProcessed;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShopStatusDistribution {
        private Long activeShops;
        private Long inactiveShops;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkersDistribution {
        private Long shops1to3;
        private Long shops4to6;
        private Long shops7to10;
        private Long shops10Plus;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyShopRegistration {
        private String month;
        private Long shopsRegistered;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyOrdersProcessed {
        private String month;
        private Long ordersProcessed;
    }
}
