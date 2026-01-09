package com.stitcho.beta.Repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.stitcho.beta.entity.OrderActivity;

@Repository
public interface OrderActivityRepository extends JpaRepository<OrderActivity, Long> {
    
    // Get recent activities for a customer (by customer ID)
    @Query("SELECT oa FROM OrderActivity oa WHERE oa.order.customer.id = :customerId ORDER BY oa.createdAt DESC")
    List<OrderActivity> findRecentActivitiesByCustomerId(@Param("customerId") Long customerId, Pageable pageable);
    
    // Get recent activities for a customer (by user ID)
    @Query("SELECT oa FROM OrderActivity oa WHERE oa.order.customer.user.id = :userId ORDER BY oa.createdAt DESC")
    List<OrderActivity> findRecentActivitiesByUserId(@Param("userId") Long userId, Pageable pageable);
    
    // Get activities for a specific order
    List<OrderActivity> findByOrder_OrderIdOrderByCreatedAtDesc(Long orderId);
}
