package com.stitcho.beta.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stitcho.beta.Repository.CustomerRepository;
import com.stitcho.beta.Repository.OrderRepository;
import com.stitcho.beta.Repository.OwnerRepository;
import com.stitcho.beta.Repository.ShopRepository;
import com.stitcho.beta.Repository.TaskRepository;
import com.stitcho.beta.Repository.UserRepository;
import com.stitcho.beta.Repository.WorkerRatingRepository;
import com.stitcho.beta.Repository.WorkerRepository;
import com.stitcho.beta.dto.OwnerProfileResponse;
import com.stitcho.beta.dto.ShopAnalyticsResponse;
import com.stitcho.beta.dto.MonthlyRevenueResponse;
import com.stitcho.beta.dto.UpdateOwnerProfileRequest;
import com.stitcho.beta.entity.Order;
import com.stitcho.beta.entity.OrderStatus;
import com.stitcho.beta.entity.Owner;
import com.stitcho.beta.entity.Shop;
import com.stitcho.beta.entity.Task;
import com.stitcho.beta.entity.TaskStatus;
import com.stitcho.beta.entity.User;
import com.stitcho.beta.entity.Worker;

import lombok.RequiredArgsConstructor;

/**
 * 🔐 SECURE OWNER SERVICE
 * Owner can view/update their own shop profile
 * shopId extracted from JWT (no manual passing)
 */
@Service
@RequiredArgsConstructor
public class SecureOwnerService {
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final OwnerRepository ownerRepository;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final WorkerRepository workerRepository;
    private final TaskRepository taskRepository;
    private final WorkerRatingRepository workerRatingRepository;

    public OwnerProfileResponse getMyShopProfile(Long userId) {
        Owner owner = ownerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        User user = owner.getUser();
        Shop shop = owner.getShop();

        OwnerProfileResponse response = new OwnerProfileResponse();
        response.setUserId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setContactNumber(user.getContactNumber());
        response.setProfilePicture(user.getProfilePicture());

        response.setShopId(shop.getShopId());
        response.setShopName(shop.getShopName());
        response.setShopEmail(shop.getShopEmail());
        response.setShopContactNumber(shop.getShopMobileNo());
        response.setShopAddress(shop.getShopAddress());

        return response;
    }

    @Transactional
    public OwnerProfileResponse updateMyShopProfile(Long userId, UpdateOwnerProfileRequest request) {
        Owner owner = ownerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        User user = owner.getUser();
        Shop shop = owner.getShop();

        // Update user details
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getContactNumber() != null) {
            user.setContactNumber(request.getContactNumber());
        }
        if (request.getProfilePicture() != null) {
            user.setProfilePicture(request.getProfilePicture());
        }
        userRepository.save(user);

        // Update shop details
        if (request.getShopName() != null) {
            shop.setShopName(request.getShopName());
        }
        if (request.getShopEmail() != null) {
            shop.setShopEmail(request.getShopEmail());
        }
        if (request.getShopContactNumber() != null) {
            shop.setShopMobileNo(request.getShopContactNumber());
        }
        if (request.getShopAddress() != null) {
            shop.setShopAddress(request.getShopAddress());
        }
        shopRepository.save(shop);

        return getMyShopProfile(userId);
    }

    // ==================== SHOP ANALYTICS ====================

    public ShopAnalyticsResponse getShopAnalytics(Long userId) {
        Owner owner = ownerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));
        
        Long shopId = owner.getShop().getShopId();

        ShopAnalyticsResponse analytics = new ShopAnalyticsResponse();
        
        // 1. Overview Stats
        analytics.setOverview(calculateOverviewStats(shopId));
        
        // 2. Daily Order Trend (last 7 days)
        analytics.setDailyOrderTrend(calculateDailyOrderTrend(shopId));
        
        // 3. Monthly Revenue Trend (last 6 months)
        analytics.setMonthlyRevenueTrend(calculateMonthlyRevenueTrend(shopId));
        
        // 4. Order Status Distribution
        analytics.setOrderStatusDistribution(calculateOrderStatusDistribution(shopId));
        
        // 5. Worker Performance
        analytics.setWorkerPerformance(calculateWorkerPerformance(shopId));
        
        return analytics;
    }

    private ShopAnalyticsResponse.OverviewStats calculateOverviewStats(Long shopId) {
        List<Order> allOrders = orderRepository.findByShop_ShopId(shopId);
        
        long totalOrders = allOrders.size();
        long completedOrders = allOrders.stream()
                .filter(o -> OrderStatus.COMPLETED.equals(o.getStatus()))
                .count();
        long activeOrders = totalOrders - completedOrders;
        
        double totalRevenue = allOrders.stream()
                .filter(o -> OrderStatus.COMPLETED.equals(o.getStatus()))
                .mapToDouble(Order::getTotalPrice)
                .sum();
        
        double pendingPayments = allOrders.stream()
                .filter(o -> !"PAID".equalsIgnoreCase(o.getPaymentStatus()))
                .mapToDouble(o -> o.getTotalPrice() - (o.getPaidAmount() != null ? o.getPaidAmount() : 0.0))
                .sum();
        
        int totalCustomers = customerRepository.findByShop_ShopId(shopId).size();
        int totalWorkers = workerRepository.findByShop_ShopId(shopId).size();
        
        return new ShopAnalyticsResponse.OverviewStats(
            totalOrders, activeOrders, completedOrders, 
            totalRevenue, pendingPayments, totalCustomers, totalWorkers
        );
    }

    private List<ShopAnalyticsResponse.DailyOrderTrend> calculateDailyOrderTrend(Long shopId) {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        
        List<Order> orders = orderRepository.findByShop_ShopId(shopId);
        
        // Group orders by day of week
        Map<DayOfWeek, Integer> ordersByDay = new HashMap<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            ordersByDay.put(day, 0);
        }
        
        for (Order order : orders) {
            if (order.getCreatedAt() != null) {
                LocalDate orderDate = order.getCreatedAt().toLocalDate();
                if (!orderDate.isBefore(startOfWeek) && !orderDate.isAfter(today)) {
                    DayOfWeek dayOfWeek = orderDate.getDayOfWeek();
                    ordersByDay.put(dayOfWeek, ordersByDay.get(dayOfWeek) + 1);
                }
            }
        }
        
        // Convert to response format
        List<ShopAnalyticsResponse.DailyOrderTrend> trend = new ArrayList<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            String dayName = day.getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            trend.add(new ShopAnalyticsResponse.DailyOrderTrend(dayName, ordersByDay.get(day)));
        }
        
        return trend;
    }

    private List<ShopAnalyticsResponse.MonthlyRevenueTrend> calculateMonthlyRevenueTrend(Long shopId) {
        LocalDate today = LocalDate.now();
        List<Order> orders = orderRepository.findByShop_ShopId(shopId);
        
        List<ShopAnalyticsResponse.MonthlyRevenueTrend> trend = new ArrayList<>();
        
        // Last 6 months
        for (int i = 5; i >= 0; i--) {
            LocalDate monthDate = today.minusMonths(i);
            String monthName = monthDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            
            double revenue = orders.stream()
                    .filter(o -> o.getCreatedAt() != null)
                    .filter(o -> {
                        LocalDate orderDate = o.getCreatedAt().toLocalDate();
                        return orderDate.getYear() == monthDate.getYear() 
                            && orderDate.getMonth() == monthDate.getMonth();
                    })
                    .filter(o -> OrderStatus.COMPLETED.equals(o.getStatus()))
                    .mapToDouble(Order::getTotalPrice)
                    .sum();
            
            // For now, expense is 0 (can be enhanced later)
            double expense = 0.0;
            
            trend.add(new ShopAnalyticsResponse.MonthlyRevenueTrend(monthName, revenue, expense));
        }
        
        return trend;
    }

    private ShopAnalyticsResponse.OrderStatusDistribution calculateOrderStatusDistribution(Long shopId) {
        List<Order> orders = orderRepository.findByShop_ShopId(shopId);
        
        int pending = (int) orders.stream().filter(o -> OrderStatus.NEW.equals(o.getStatus())).count();
        int cutting = (int) orders.stream().filter(o -> OrderStatus.CUTTING.equals(o.getStatus())).count();
        int stitching = (int) orders.stream().filter(o -> OrderStatus.STITCHING.equals(o.getStatus())).count();
        int fitting = (int) orders.stream().filter(o -> OrderStatus.IRONING.equals(o.getStatus())).count();
        int ready = 0; // No READY status in enum
        int completed = (int) orders.stream().filter(o -> OrderStatus.COMPLETED.equals(o.getStatus())).count();
        
        return new ShopAnalyticsResponse.OrderStatusDistribution(
            pending, cutting, stitching, fitting, ready, completed, orders.size()
        );
    }

    private List<ShopAnalyticsResponse.WorkerPerformance> calculateWorkerPerformance(Long shopId) {
        List<Worker> workers = workerRepository.findByShop_ShopId(shopId);
        
        return workers.stream()
                .map(worker -> {
                    List<Task> tasks = taskRepository.findByWorker_Id(worker.getId());
                    int totalTasks = tasks.size();
                    int completedTasks = (int) tasks.stream()
                            .filter(t -> TaskStatus.COMPLETED.equals(t.getStatus()))
                            .count();
                    
                    double completionRate = totalTasks > 0 
                            ? (completedTasks * 100.0 / totalTasks) 
                            : 0.0;
                    
                    // Get average rating
                    Double avgRating = workerRatingRepository.getAverageRatingByWorkerId(worker.getId());
                    
                    // Performance percentage (based on completion rate and rating)
                    double performancePercentage = totalTasks > 0 
                            ? completionRate 
                            : 0.0;
                    
                    return new ShopAnalyticsResponse.WorkerPerformance(
                        worker.getId(),
                        worker.getUser().getName(),
                        worker.getWorkType(),
                        totalTasks,
                        completedTasks,
                        Math.round(performancePercentage * 10.0) / 10.0,
                        avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0,
                        Math.round(completionRate * 10.0) / 10.0
                    );
                })
                .collect(Collectors.toList());
    }

    // ==================== MONTHLY REVENUE ====================

    public MonthlyRevenueResponse getMonthlyRevenue(Long userId, Integer year) {
        Owner owner = ownerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));
        
        Long shopId = owner.getShop().getShopId();
        
        // Use current year if not specified
        int targetYear = year != null ? year : LocalDate.now().getYear();
        
        // Get all completed orders for the shop
        List<Order> allOrders = orderRepository.findByShop_ShopId(shopId);
        List<Order> completedOrders = allOrders.stream()
                .filter(o -> OrderStatus.COMPLETED.equals(o.getStatus()))
                .filter(o -> o.getCreatedAt() != null)
                .filter(o -> o.getCreatedAt().getYear() == targetYear)
                .collect(Collectors.toList());
        
        // Group orders by month and calculate revenue
        List<MonthlyRevenueResponse.MonthRevenue> monthlyData = new ArrayList<>();
        double totalYearRevenue = 0.0;
        String highestRevenueMonth = "";
        double highestRevenueAmount = 0.0;
        
        for (int month = 1; month <= 12; month++) {
            final int currentMonth = month;
            
            List<Order> monthOrders = completedOrders.stream()
                    .filter(o -> o.getCreatedAt().getMonthValue() == currentMonth)
                    .collect(Collectors.toList());
            
            double monthRevenue = monthOrders.stream()
                    .mapToDouble(Order::getTotalPrice)
                    .sum();
            
            int orderCount = monthOrders.size();
            double avgOrderValue = orderCount > 0 ? monthRevenue / orderCount : 0.0;
            
            String monthName = java.time.Month.of(month)
                    .getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH);
            
            monthlyData.add(new MonthlyRevenueResponse.MonthRevenue(
                month,
                monthName,
                Math.round(monthRevenue * 100.0) / 100.0,
                orderCount,
                Math.round(avgOrderValue * 100.0) / 100.0
            ));
            
            totalYearRevenue += monthRevenue;
            
            // Track highest revenue month
            if (monthRevenue > highestRevenueAmount) {
                highestRevenueAmount = monthRevenue;
                highestRevenueMonth = monthName;
            }
        }
        
        double averageMonthlyRevenue = totalYearRevenue / 12;
        
        return new MonthlyRevenueResponse(
            targetYear,
            monthlyData,
            Math.round(totalYearRevenue * 100.0) / 100.0,
            Math.round(averageMonthlyRevenue * 100.0) / 100.0,
            highestRevenueMonth,
            Math.round(highestRevenueAmount * 100.0) / 100.0
        );
    }
}
