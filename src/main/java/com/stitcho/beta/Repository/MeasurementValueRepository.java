package com.stitcho.beta.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import com.stitcho.beta.entity.MeasurementValue;

@Repository
public interface MeasurementValueRepository extends JpaRepository<MeasurementValue, Long> {
    
    // Find all values for a profile
    List<MeasurementValue> findByProfile_Id(Long profileId);
    
    // Find specific value by profile and key
    Optional<MeasurementValue> findByProfile_IdAndMeasurementKey(Long profileId, String measurementKey);
    
    // Find all values with a specific key across all profiles
    List<MeasurementValue> findByMeasurementKey(String measurementKey);
    
    // Delete all values for a profile
    @Modifying
    void deleteByProfile_Id(Long profileId);
    
    // Delete specific value
    @Modifying
    void deleteByProfile_IdAndMeasurementKey(Long profileId, String measurementKey);
}
