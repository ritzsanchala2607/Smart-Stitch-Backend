package com.stitcho.beta.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AllShopsResponse {
    private Long shopId;
    private String shopName;
    private String ownerName;
    private String ownerEmail;
    private String ownerContact;
    private Long totalOrders;
    private Long totalWorkers;
    private String shopAddress;
    private LocalDateTime createdAt;
    private Boolean isActive;
}
