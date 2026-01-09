package com.stitcho.beta.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShopInfoResponse {
    private Long shopId;
    private String shopName;
    private String email;
    private String contactNumber;
    private String address;
    private Double averageRating;
    private Long totalRatings;
}
