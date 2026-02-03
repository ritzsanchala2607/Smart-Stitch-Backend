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
public class ShopAnalyticsResponse {
    private ShopStatusDistribution shopStatusDistribution;
    private WorkersDistribution workersDistribution;
    private List<MonthlyShopRegistration> monthlyShopRegistrations;
    private List<MonthlyOrdersProcessed> monthlyOrdersProcessed;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShopStatusDistribution {
        private Long activeShops;
        private Long inactiveShops;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkersDistribution {
        private Long shops1to3Workers;
        private Long shops4to6Workers;
        private Long shops7to10Workers;
        private Long shops10PlusWorkers;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyShopRegistration {
        private String month;
        private Long shopsRegistered;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyOrdersProcessed {
        private String month;
        private Long ordersProcessed;
    }
}
