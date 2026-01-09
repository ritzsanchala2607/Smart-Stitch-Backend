package com.stitcho.beta.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.stitcho.beta.entity.ShopRating;

@Repository
public interface ShopRatingRepository extends JpaRepository<ShopRating, Long> {
    
    // Find all ratings for a shop
    List<ShopRating> findByShop_ShopId(Long shopId);
    
    // Find rating by shop and order
    Optional<ShopRating> findByShop_ShopIdAndOrder_OrderId(Long shopId, Long orderId);
    
    // Check if rating exists for order
    boolean existsByShop_ShopIdAndOrder_OrderId(Long shopId, Long orderId);
    
    // Get average rating for a shop
    @Query("SELECT AVG(sr.rating) FROM ShopRating sr WHERE sr.shop.shopId = :shopId")
    Double getAverageRatingByShopId(@Param("shopId") Long shopId);
    
    // Get rating count for a shop
    @Query("SELECT COUNT(sr) FROM ShopRating sr WHERE sr.shop.shopId = :shopId")
    Long getRatingCountByShopId(@Param("shopId") Long shopId);
}
