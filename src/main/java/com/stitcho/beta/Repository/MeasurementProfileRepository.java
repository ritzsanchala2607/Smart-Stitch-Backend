package com.stitcho.beta.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.stitcho.beta.entity.DressType;
import com.stitcho.beta.entity.MeasurementProfile;

@Repository
public interface MeasurementProfileRepository extends JpaRepository<MeasurementProfile, Long> {
    
    // Find all profiles for a customer
    List<MeasurementProfile> findByCustomer_Id(Long customerId);
    
    // Find specific profile by customer and dress type
    Optional<MeasurementProfile> findByCustomer_IdAndDressType(Long customerId, DressType dressType);
    
    // Find all profiles of a specific dress type
    List<MeasurementProfile> findByDressType(DressType dressType);
    
    // Find profiles by customer and shop
    @Query("SELECT mp FROM MeasurementProfile mp WHERE mp.customer.id = :customerId AND mp.customer.shop.shopId = :shopId")
    List<MeasurementProfile> findByCustomerIdAndShopId(@Param("customerId") Long customerId, @Param("shopId") Long shopId);
    
    // Check if profile exists
    boolean existsByCustomer_IdAndDressType(Long customerId, DressType dressType);
    
    // Delete by customer and dress type
    void deleteByCustomer_IdAndDressType(Long customerId, DressType dressType);
}
