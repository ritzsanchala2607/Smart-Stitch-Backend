package com.stitcho.beta.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillResponse {
    private String billNumber;
    private LocalDateTime billDate;
    private ShopInfo shop;
    private CustomerInfo customer;
    private OrderInfo order;
    private List<ItemInfo> items;
    private PricingInfo pricing;
    private PaymentInfo payment;
    private String notes;
    private String terms;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShopInfo {
        private Long shopId;
        private String name;
        private String address;
        private String contactNumber;
        private String email;
        private String gstNumber;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerInfo {
        private Long customerId;
        private String name;
        private String contactNumber;
        private String email;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderInfo {
        private Long orderId;
        private String orderNumber;
        private LocalDateTime orderDate;
        private LocalDate deadline;
        private String status;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemInfo {
        private Long itemId;
        private String name;
        private Integer quantity;
        private Double unitPrice;
        private Double totalPrice;
        private String fabricType;
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PricingInfo {
        private Double subtotal;
        private Double discount;
        private Double taxRate;
        private Double taxAmount;
        private Double totalAmount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentInfo {
        private Double totalPrice;
        private Double paidAmount;
        private Double balanceAmount;
        private String paymentStatus;
        private List<PaymentDetail> payments;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentDetail {
        private Double amount;
        private String method;
        private LocalDateTime date;
        private String note;
    }
}
