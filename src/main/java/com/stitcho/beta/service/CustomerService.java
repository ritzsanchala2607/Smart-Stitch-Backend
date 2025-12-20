package com.stitcho.beta.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stitcho.beta.Repository.CustomerMeasurementRepository;
import com.stitcho.beta.Repository.CustomerRepository;
import com.stitcho.beta.Repository.RoleRepository;
import com.stitcho.beta.Repository.ShopRepository;
import com.stitcho.beta.Repository.UserRepository;
import com.stitcho.beta.dto.CreateCustomerRequest;
import com.stitcho.beta.dto.CreateCustomerResponse;
import com.stitcho.beta.dto.CustomerResponse;
import com.stitcho.beta.dto.MeasurementDto;
import com.stitcho.beta.dto.UpdateCustomerRequest;
import com.stitcho.beta.entity.Customer;
import com.stitcho.beta.entity.CustomerMeasurement;
import com.stitcho.beta.entity.Role;
import com.stitcho.beta.entity.Shop;
import com.stitcho.beta.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final CustomerMeasurementRepository measurementRepository;
    private final ShopRepository shopRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public CreateCustomerResponse createCustomer(Long shopId, CreateCustomerRequest request) {
        // 1️⃣ Validate shop
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        // 2️⃣ Check if email already exists
        if (userRepository.findByEmail(request.getUser().getEmail()) != null) {
            throw new IllegalArgumentException("Email already in use");
        }

        // 3️⃣ Create user with CUSTOMER role
        Role customerRole = roleRepository.findById(request.getUser().getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        User user = new User();
        user.setName(request.getUser().getName());
        user.setEmail(request.getUser().getEmail());
        user.setContactNumber(request.getUser().getContactNumber());
        user.setPassword(passwordEncoder.encode(request.getUser().getPassword()));
        user.setProfilePicture(request.getUser().getProfilePicture());
        user.setRole(customerRole);
        user = userRepository.save(user);

        // 4️⃣ Create customer
        Customer customer = new Customer();
        customer.setUser(user);
        customer.setShop(shop);
        customer = customerRepository.save(customer);

        // 5️⃣ Create measurements (optional)
        if (request.getMeasurements() != null) {
            CustomerMeasurement measurement = new CustomerMeasurement();
            measurement.setCustomer(customer);
            measurement.setChest(request.getMeasurements().getChest());
            measurement.setShoulder(request.getMeasurements().getShoulder());
            measurement.setShirtLength(request.getMeasurements().getShirtLength());
            measurement.setWaist(request.getMeasurements().getWaist());
            measurement.setPantLength(request.getMeasurements().getPantLength());
            measurement.setHip(request.getMeasurements().getHip());
            measurement.setCustomMeasurements(request.getMeasurements().getCustomMeasurements());
            measurementRepository.save(measurement);
        }

        return new CreateCustomerResponse(customer.getId(), user.getId());
    }

    public CustomerResponse getCustomer(Long shopId, Long customerId) {
        Customer customer = customerRepository.findByIdAndShop_ShopId(customerId, shopId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        CustomerResponse response = new CustomerResponse();
        response.setCustomerId(customer.getId());
        response.setCreatedAt(customer.getCreatedAt());

        // User info
        User user = customer.getUser();
        CustomerResponse.UserInfo userInfo = new CustomerResponse.UserInfo();
        userInfo.setName(user.getName());
        userInfo.setEmail(user.getEmail());
        userInfo.setContactNumber(user.getContactNumber());
        userInfo.setProfilePicture(user.getProfilePicture());
        response.setUser(userInfo);

        // Measurements
        measurementRepository.findByCustomer_Id(customerId).ifPresent(measurement -> {
            MeasurementDto measurementDto = new MeasurementDto();
            measurementDto.setChest(measurement.getChest());
            measurementDto.setShoulder(measurement.getShoulder());
            measurementDto.setShirtLength(measurement.getShirtLength());
            measurementDto.setWaist(measurement.getWaist());
            measurementDto.setPantLength(measurement.getPantLength());
            measurementDto.setHip(measurement.getHip());
            measurementDto.setCustomMeasurements(measurement.getCustomMeasurements());
            response.setMeasurements(measurementDto);
        });

        return response;
    }

    @Transactional
    public void updateCustomer(Long shopId, Long customerId, UpdateCustomerRequest request) {
        Customer customer = customerRepository.findByIdAndShop_ShopId(customerId, shopId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Update user info
        if (request.getUser() != null) {
            User user = customer.getUser();
            if (request.getUser().getName() != null) {
                user.setName(request.getUser().getName());
            }
            if (request.getUser().getContactNumber() != null) {
                user.setContactNumber(request.getUser().getContactNumber());
            }
            if (request.getUser().getProfilePicture() != null) {
                user.setProfilePicture(request.getUser().getProfilePicture());
            }
            userRepository.save(user);
        }

        // Upsert measurements
        if (request.getMeasurements() != null) {
            CustomerMeasurement measurement = measurementRepository.findByCustomer_Id(customerId)
                    .orElse(new CustomerMeasurement());

            measurement.setCustomer(customer);
            if (request.getMeasurements().getChest() != null) {
                measurement.setChest(request.getMeasurements().getChest());
            }
            if (request.getMeasurements().getShoulder() != null) {
                measurement.setShoulder(request.getMeasurements().getShoulder());
            }
            if (request.getMeasurements().getShirtLength() != null) {
                measurement.setShirtLength(request.getMeasurements().getShirtLength());
            }
            if (request.getMeasurements().getWaist() != null) {
                measurement.setWaist(request.getMeasurements().getWaist());
            }
            if (request.getMeasurements().getPantLength() != null) {
                measurement.setPantLength(request.getMeasurements().getPantLength());
            }
            if (request.getMeasurements().getHip() != null) {
                measurement.setHip(request.getMeasurements().getHip());
            }
            if (request.getMeasurements().getCustomMeasurements() != null) {
                measurement.setCustomMeasurements(request.getMeasurements().getCustomMeasurements());
            }
            measurementRepository.save(measurement);
        }
    }
}
