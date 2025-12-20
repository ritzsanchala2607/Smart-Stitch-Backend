package com.stitcho.beta.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stitcho.beta.entity.CustomerMeasurement;

@Repository
public interface CustomerMeasurementRepository extends JpaRepository<CustomerMeasurement, Long> {
    Optional<CustomerMeasurement> findByCustomer_Id(Long customerId);
}
