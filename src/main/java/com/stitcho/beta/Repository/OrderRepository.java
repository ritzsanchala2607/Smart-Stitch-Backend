package com.stitcho.beta.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stitcho.beta.entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderIdAndShop_ShopId(Long orderId, Long shopId);
    List<Order> findByCustomer_IdAndShop_ShopId(Long customerId, Long shopId);
    List<Order> findByShop_ShopId(Long shopId);
    List<Order> findByShop_ShopIdAndCreatedAtBetween(Long shopId, LocalDateTime startDate, LocalDateTime endDate);
}
