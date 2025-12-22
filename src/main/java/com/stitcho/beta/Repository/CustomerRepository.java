package com.stitcho.beta.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stitcho.beta.entity.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByIdAndShop_ShopId(Long customerId, Long shopId);
    Optional<Customer> findByUser_Id(Long userId);
    List<Customer> findByShop_ShopId(Long shopId);
    List<Customer> findByShop_ShopIdAndUser_NameContainingIgnoreCase(Long shopId, String name);
}
