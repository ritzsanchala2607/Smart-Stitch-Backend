package com.stitcho.beta.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stitcho.beta.Repository.CustomerRepository;
import com.stitcho.beta.Repository.OrderRepository;
import com.stitcho.beta.Repository.OwnerRepository;
import com.stitcho.beta.Repository.RoleRepository;
import com.stitcho.beta.Repository.ShopRatingRepository;
import com.stitcho.beta.Repository.ShopRepository;
import com.stitcho.beta.Repository.UserRepository;
import com.stitcho.beta.dto.CreateCustomerRequest;
import com.stitcho.beta.dto.CreateCustomerResponse;
import com.stitcho.beta.dto.CustomerResponse;
import com.stitcho.beta.dto.CustomerStatsResponse;
import com.stitcho.beta.dto.OrderResponse;
import com.stitcho.beta.dto.PaymentHistoryResponse;
import com.stitcho.beta.dto.ShopInfoResponse;
import com.stitcho.beta.dto.UpdateCustomerRequest;
import com.stitcho.beta.entity.Customer;
import com.stitcho.beta.entity.Order;
import com.stitcho.beta.entity.OrderStatus;
import com.stitcho.beta.entity.Owner;
import com.stitcho.beta.entity.Role;
import com.stitcho.beta.entity.Shop;
import com.stitcho.beta.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SecureCustomerService {
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final RoleRepository roleRepository;
    private final OwnerRepository ownerRepository;
    private final OrderRepository orderRepository;
    private final ShopRatingRepository shopRatingRepository;
    private final ShopRepository shopRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureOrderService orderService;

    @Transactional
    public CreateCustomerResponse createCustomer(Long userId, CreateCustomerRequest request) {
        // Get owner's shop
        Owner owner = ownerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));
        
        Shop shop = owner.getShop();

        // Check if email already exists
        if (userRepository.findByEmail(request.getUser().getEmail()) != null) {
            throw new IllegalArgumentException("Email already in use");
        }

        // Create user with CUSTOMER role
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

        // Create customer
        Customer customer = new Customer();
        customer.setUser(user);
        customer.setShop(shop);
        customer = customerRepository.save(customer);

        // Note: Measurements are now managed through the Measurement API
        // Use POST /api/measurements to create measurement profiles for different dress types

        return new CreateCustomerResponse(customer.getId(), user.getId());
    }

    public CustomerResponse getCustomer(Long userId, String role, Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Access control
        if ("OWNER".equalsIgnoreCase(role)) {
            Owner owner = ownerRepository.findByUser_Id(userId)
                    .orElseThrow(() -> new RuntimeException("Owner not found"));
            if (!customer.getShop().getShopId().equals(owner.getShop().getShopId())) {
                throw new RuntimeException("Access denied");
            }
        } else if ("CUSTOMER".equalsIgnoreCase(role)) {
            if (customer.getUser().getId() != userId) {
                throw new RuntimeException("Access denied");
            }
        }

        return mapToCustomerResponse(customer);
    }

    public CustomerResponse getMyProfile(Long userId) {
        // Find customer by userId - customer table uses user_id as foreign key
        Customer customer = customerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        return mapToCustomerResponse(customer);
    }

    @Transactional
    public void updateCustomer(Long userId, String role, Long customerId, UpdateCustomerRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Access control
        if ("OWNER".equalsIgnoreCase(role)) {
            Owner owner = ownerRepository.findByUser_Id(userId)
                    .orElseThrow(() -> new RuntimeException("Owner not found"));
            if (!customer.getShop().getShopId().equals(owner.getShop().getShopId())) {
                throw new RuntimeException("Access denied");
            }
        } else if ("CUSTOMER".equalsIgnoreCase(role)) {
            if (customer.getUser().getId() != userId) {
                throw new RuntimeException("Access denied");
            }
        }

        // Update user info (both owner and customer can update)
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

        // Note: Measurements are now managed through the Measurement API
        // Use PUT /api/measurements/{profileId} to update measurement profiles
    }

    public List<OrderResponse> getMyOrders(Long userId) {
        // Find customer by userId
        Customer customer = customerRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        List<Order> orders = orderRepository.findByCustomer_IdAndShop_ShopId(
            customer.getId(), 
            customer.getShop().getShopId()
        );

        return orders.stream()
                .map(order -> orderService.getOrder(userId, "CUSTOMER", order.getOrderId()))
                .toList();
    }

    public List<CustomerResponse> getAllCustomers(Long userId, String name) {
        // Get owner's shop
        Owner owner = ownerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));
        
        Long shopId = owner.getShop().getShopId();

        List<Customer> customers;
        if (name != null && !name.trim().isEmpty()) {
            customers = customerRepository.findByShop_ShopIdAndUser_NameContainingIgnoreCase(shopId, name);
        } else {
            customers = customerRepository.findByShop_ShopId(shopId);
        }

        return customers.stream()
                .map(this::mapToCustomerResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional
    public void deleteCustomer(Long userId, Long customerId) {
        // Get owner's shop
        Owner owner = ownerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));
        
        // Get customer and verify it belongs to owner's shop
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        if (!customer.getShop().getShopId().equals(owner.getShop().getShopId())) {
            throw new RuntimeException("Access denied: Customer does not belong to your shop");
        }

        // Delete customer (cascade will handle orders, tasks, measurement profiles, etc.)
        customerRepository.delete(customer);
        
        // Delete associated user account
        userRepository.delete(customer.getUser());
    }

    private CustomerResponse mapToCustomerResponse(Customer customer) {
        CustomerResponse response = new CustomerResponse();
        response.setCustomerId(customer.getId());
        response.setCreatedAt(customer.getCreatedAt());

        // User info
        if (customer.getUser() != null) {
            User user = customer.getUser();
            CustomerResponse.UserInfo userInfo = new CustomerResponse.UserInfo();
            userInfo.setName(user.getName());
            userInfo.setEmail(user.getEmail());
            userInfo.setContactNumber(user.getContactNumber());
            userInfo.setProfilePicture(user.getProfilePicture());
            response.setUser(userInfo);
        }

        // Note: Measurements are now retrieved through the Measurement API
        // Use GET /api/measurements/customer/{customerId} to get all measurement profiles

        return response;
    }

    // ==================== CUSTOMER DASHBOARD METHODS ====================

    /**
     * Get customer dashboard statistics
     */
    public CustomerStatsResponse getCustomerStats(Long userId) {
        // Find customer by userId
        Customer customer = customerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Get all orders for this customer
        List<Order> orders = orderRepository.findByCustomer_Id(customer.getId());

        // Calculate statistics
        long totalOrders = orders.size();
        long completedOrders = orders.stream()
                .filter(order -> OrderStatus.COMPLETED.equals(order.getStatus()))
                .count();
        long activeOrders = totalOrders - completedOrders;

        double totalSpent = orders.stream()
                .filter(order -> OrderStatus.COMPLETED.equals(order.getStatus()))
                .mapToDouble(Order::getTotalPrice)
                .sum();

        double pendingPayment = orders.stream()
                .filter(order -> !OrderStatus.COMPLETED.equals(order.getStatus()) 
                        && !"PAID".equalsIgnoreCase(order.getPaymentStatus()))
                .mapToDouble(order -> order.getTotalPrice() - (order.getPaidAmount() != null ? order.getPaidAmount() : 0.0))
                .sum();

        return new CustomerStatsResponse(totalOrders, activeOrders, completedOrders, totalSpent, pendingPayment);
    }

    /**
     * Get customer payment history
     */
    public List<PaymentHistoryResponse> getPaymentHistory(Long userId) {
        // Find customer by userId
        Customer customer = customerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Get all orders for this customer
        List<Order> orders = orderRepository.findByCustomer_Id(customer.getId());

        return orders.stream()
                .map(order -> new PaymentHistoryResponse(
                    order.getOrderId(),
                    order.getTotalPrice(),
                    order.getPaidAmount(),
                    order.getPaymentStatus(),
                    order.getCreatedAt(),
                    order.getNotes()
                ))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get shop information with ratings
     */
    public ShopInfoResponse getShopInfo(Long shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        // Get shop ratings
        Double avgRating = shopRatingRepository.getAverageRatingByShopId(shopId);
        Long totalRatings = shopRatingRepository.getRatingCountByShopId(shopId);

        return new ShopInfoResponse(
            shop.getShopId(),
            shop.getShopName(),
            shop.getShopEmail(),
            shop.getShopMobileNo(),
            shop.getShopAddress(),
            avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0,
            totalRatings != null ? totalRatings : 0L
        );
    }

    /**
     * Get customer order history with date filtering
     */
    public List<OrderResponse> getOrderHistory(Long userId, Integer year, Integer month, 
                                                 String startDate, String endDate) {
        // Find customer by userId
        Customer customer = customerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Get all orders for this customer
        List<Order> orders = orderRepository.findByCustomer_Id(customer.getId());

        // Filter by date if parameters provided
        if (year != null || month != null || startDate != null || endDate != null) {
            orders = orders.stream()
                    .filter(order -> matchesDateFilter(order, year, month, startDate, endDate))
                    .collect(java.util.stream.Collectors.toList());
        }

        return orders.stream()
                .map(order -> orderService.getOrder(userId, "CUSTOMER", order.getOrderId()))
                .collect(java.util.stream.Collectors.toList());
    }

    private boolean matchesDateFilter(Order order, Integer year, Integer month, 
                                       String startDate, String endDate) {
        if (order.getCreatedAt() == null) {
            return false;
        }

        // Year filter
        if (year != null && order.getCreatedAt().getYear() != year) {
            return false;
        }

        // Month filter
        if (month != null && order.getCreatedAt().getMonthValue() != month) {
            return false;
        }

        // Date range filter
        if (startDate != null) {
            try {
                java.time.LocalDate start = java.time.LocalDate.parse(startDate);
                if (order.getCreatedAt().toLocalDate().isBefore(start)) {
                    return false;
                }
            } catch (Exception e) {
                // Invalid date format, skip filter
            }
        }

        if (endDate != null) {
            try {
                java.time.LocalDate end = java.time.LocalDate.parse(endDate);
                if (order.getCreatedAt().toLocalDate().isAfter(end)) {
                    return false;
                }
            } catch (Exception e) {
                // Invalid date format, skip filter
            }
        }

        return true;
    }
}
