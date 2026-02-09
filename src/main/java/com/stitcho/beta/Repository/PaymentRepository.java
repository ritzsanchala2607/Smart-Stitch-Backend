package com.stitcho.beta.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stitcho.beta.entity.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByOrder_OrderIdOrderByPaymentDateDesc(Long orderId);
    List<Payment> findByOrder_Shop_ShopIdOrderByPaymentDateDesc(Long shopId);
}
