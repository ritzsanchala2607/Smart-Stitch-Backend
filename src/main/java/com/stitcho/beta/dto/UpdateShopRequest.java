package com.stitcho.beta.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateShopRequest {
    private ShopDetails shop;
    private OwnerDetails owner;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShopDetails {
        private String shopName;
        private String shopEmail;
        private String shopMobileNo;
        private String shopAddress;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OwnerDetails {
        private String name;
        private String email;
        private String contactNumber;
    }
}
