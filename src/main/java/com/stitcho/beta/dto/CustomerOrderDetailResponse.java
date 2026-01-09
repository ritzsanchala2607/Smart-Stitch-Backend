package com.stitcho.beta.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerOrderDetailResponse {
    private Long orderId;
    private String orderStatus;
    private LocalDate deadline;
    private Double totalPrice;
    private Double paidAmount;
    private String paymentStatus;
    private String notes;
    private LocalDateTime createdAt;
    
    // Shop Information
    private ShopInfo shop;
    
    // Order Items
    private List<OrderItemInfo> items;
    
    // Tasks with Worker Names
    private List<TaskWithWorkerInfo> tasks;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShopInfo {
        private Long shopId;
        private String shopName;
        private String shopEmail;
        private String shopContactNumber;
        private String shopAddress;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemInfo {
        private Long itemId;
        private String itemName;
        private Integer quantity;
        private Double price;
        private String fabricType;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskWithWorkerInfo {
        private Long taskId;
        private String taskType;
        private String taskStatus;
        private Long workerId;
        private String workerName;
        private String workerContactNumber;
        private LocalDateTime assignedAt;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
    }
}
