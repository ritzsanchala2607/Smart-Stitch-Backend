package com.stitcho.beta.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stitcho.beta.Repository.CustomerRepository;
import com.stitcho.beta.Repository.MeasurementProfileRepository;
import com.stitcho.beta.Repository.MeasurementValueRepository;
import com.stitcho.beta.dto.MeasurementProfileRequest;
import com.stitcho.beta.dto.MeasurementProfileResponse;
import com.stitcho.beta.entity.Customer;
import com.stitcho.beta.entity.DressType;
import com.stitcho.beta.entity.MeasurementProfile;
import com.stitcho.beta.entity.MeasurementValue;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MeasurementService {
    
    private final MeasurementProfileRepository profileRepository;
    private final MeasurementValueRepository valueRepository;
    private final CustomerRepository customerRepository;

    @Transactional
    public MeasurementProfileResponse createProfile(MeasurementProfileRequest request) {
        // Validate customer exists
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Check if profile already exists for this dress type
        profileRepository.findByCustomer_IdAndDressType(request.getCustomerId(), request.getDressType())
                .ifPresent(p -> {
                    throw new RuntimeException("Measurement profile already exists for this dress type");
                });

        // Create new profile
        MeasurementProfile profile = new MeasurementProfile();
        profile.setCustomer(customer);
        profile.setDressType(request.getDressType());
        profile.setNotes(request.getNotes());

        // Save profile first
        profile = profileRepository.save(profile);

        // Add measurements
        for (Map.Entry<String, Double> entry : request.getMeasurements().entrySet()) {
            MeasurementValue value = new MeasurementValue();
            value.setProfile(profile);
            value.setMeasurementKey(entry.getKey().toLowerCase());
            value.setMeasurementValue(BigDecimal.valueOf(entry.getValue()));
            profile.addMeasurement(value);
        }

        // Save with measurements
        profile = profileRepository.save(profile);

        return mapToResponse(profile);
    }

    @Transactional
    public MeasurementProfileResponse createOrUpdateProfile(Long customerId, MeasurementProfileRequest request) {
        // Validate customer exists
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Get or create profile
        MeasurementProfile profile = profileRepository
                .findByCustomer_IdAndDressType(customerId, request.getDressType())
                .orElse(new MeasurementProfile());

        profile.setCustomer(customer);
        profile.setDressType(request.getDressType());
        profile.setNotes(request.getNotes());

        // Save profile first
        profile = profileRepository.save(profile);

        // Clear existing measurements
        profile.getMeasurements().clear();

        // Add new measurements
        for (Map.Entry<String, Double> entry : request.getMeasurements().entrySet()) {
            MeasurementValue value = new MeasurementValue();
            value.setProfile(profile);
            value.setMeasurementKey(entry.getKey().toLowerCase());
            value.setMeasurementValue(BigDecimal.valueOf(entry.getValue()));
            profile.addMeasurement(value);
        }

        // Save with measurements
        profile = profileRepository.save(profile);

        return mapToResponse(profile);
    }

    @Transactional(readOnly = true)
    public MeasurementProfileResponse getProfileById(Long profileId) {
        MeasurementProfile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Measurement profile not found"));

        return mapToResponse(profile);
    }

    @Transactional(readOnly = true)
    public MeasurementProfileResponse getProfile(Long customerId, DressType dressType) {
        MeasurementProfile profile = profileRepository
                .findByCustomer_IdAndDressType(customerId, dressType)
                .orElseThrow(() -> new RuntimeException("Measurement profile not found"));

        return mapToResponse(profile);
    }

    @Transactional(readOnly = true)
    public List<MeasurementProfileResponse> getAllProfilesForCustomer(Long customerId) {
        List<MeasurementProfile> profiles = profileRepository.findByCustomer_Id(customerId);
        return profiles.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public MeasurementProfileResponse updateProfile(Long profileId, MeasurementProfileRequest request) {
        MeasurementProfile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Measurement profile not found"));

        // Update notes if provided
        if (request.getNotes() != null) {
            profile.setNotes(request.getNotes());
        }

        // Update measurements if provided
        if (request.getMeasurements() != null && !request.getMeasurements().isEmpty()) {
            // Clear existing measurements
            profile.getMeasurements().clear();
            profileRepository.save(profile);

            // Add new measurements
            for (Map.Entry<String, Double> entry : request.getMeasurements().entrySet()) {
                MeasurementValue value = new MeasurementValue();
                value.setProfile(profile);
                value.setMeasurementKey(entry.getKey().toLowerCase());
                value.setMeasurementValue(BigDecimal.valueOf(entry.getValue()));
                profile.addMeasurement(value);
            }
        }

        // Save profile
        profile = profileRepository.save(profile);

        return mapToResponse(profile);
    }

    @Transactional
    public void deleteProfileById(Long profileId) {
        if (!profileRepository.existsById(profileId)) {
            throw new RuntimeException("Measurement profile not found");
        }
        profileRepository.deleteById(profileId);
    }

    @Transactional
    public void deleteProfile(Long customerId, DressType dressType) {
        profileRepository.deleteByCustomer_IdAndDressType(customerId, dressType);
    }

    @Transactional
    public void updateMeasurement(Long customerId, DressType dressType, String measurementKey, Double value) {
        MeasurementProfile profile = profileRepository
                .findByCustomer_IdAndDressType(customerId, dressType)
                .orElseThrow(() -> new RuntimeException("Measurement profile not found"));

        MeasurementValue measurement = valueRepository
                .findByProfile_IdAndMeasurementKey(profile.getId(), measurementKey.toLowerCase())
                .orElse(new MeasurementValue());

        measurement.setProfile(profile);
        measurement.setMeasurementKey(measurementKey.toLowerCase());
        measurement.setMeasurementValue(BigDecimal.valueOf(value));

        valueRepository.save(measurement);
    }

    @Transactional
    public void deleteMeasurement(Long customerId, DressType dressType, String measurementKey) {
        MeasurementProfile profile = profileRepository
                .findByCustomer_IdAndDressType(customerId, dressType)
                .orElseThrow(() -> new RuntimeException("Measurement profile not found"));

        valueRepository.deleteByProfile_IdAndMeasurementKey(profile.getId(), measurementKey.toLowerCase());
    }

    private MeasurementProfileResponse mapToResponse(MeasurementProfile profile) {
        MeasurementProfileResponse response = new MeasurementProfileResponse();
        response.setProfileId(profile.getId());
        response.setCustomerId(profile.getCustomer().getId());
        response.setCustomerName(profile.getCustomer().getUser().getName());
        response.setDressType(profile.getDressType());
        response.setNotes(profile.getNotes());
        response.setCreatedAt(profile.getCreatedAt());
        response.setUpdatedAt(profile.getUpdatedAt());

        // Convert measurements to map
        Map<String, Double> measurements = new HashMap<>();
        for (MeasurementValue value : profile.getMeasurements()) {
            measurements.put(value.getMeasurementKey(), value.getMeasurementValue().doubleValue());
        }
        response.setMeasurements(measurements);

        return response;
    }
}
