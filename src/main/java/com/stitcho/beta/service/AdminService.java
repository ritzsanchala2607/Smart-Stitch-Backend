package com.stitcho.beta.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stitcho.beta.Repository.CustomerRepository;
import com.stitcho.beta.Repository.MeasurementProfileRepository;
import com.stitcho.beta.Repository.OrderActivityRepository;
import com.stitcho.beta.Repository.OrderItemRepository;
import com.stitcho.beta.Repository.OrderRepository;
import com.stitcho.beta.Repository.OwnerRepository;
import com.stitcho.beta.Repository.RateRepository;
import com.stitcho.beta.Repository.ShopRatingRepository;
import com.stitcho.beta.Repository.ShopRepository;
import com.stitcho.beta.Repository.TaskRepository;
import com.stitcho.beta.Repository.UserRepository;
import com.stitcho.beta.Repository.WorkerRatingRepository;
import com.stitcho.beta.Repository.WorkerRepository;
import com.stitcho.beta.dto.AdminDashboardResponse;
import com.stitcho.beta.dto.AdminAnalyticsResponse;
import com.stitcho.beta.dto.AllShopsResponse;
import com.stitcho.beta.dto.PlatformAnalyticsResponse;
import com.stitcho.beta.dto.UpdateShopRequest;
import com.stitcho.beta.entity.Customer;
import com.stitcho.beta.entity.Order;
import com.stitcho.beta.entity.OrderActivity;
import com.stitcho.beta.entity.OrderItem;
import com.stitcho.beta.entity.Owner;
import com.stitcho.beta.entity.Shop;
import com.stitcho.beta.entity.Task;
import com.stitcho.beta.entity.User;
import com.stitcho.beta.entity.Worker;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final ShopRepository shopRepository;
    private final OwnerRepository ownerRepository;
    private final WorkerRepository workerRepository;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final MeasurementProfileRepository measurementProfileRepository;
    private final TaskRepository taskRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderActivityRepository orderActivityRepository;
    private final RateRepository rateRepository;
    private final WorkerRatingRepository workerRatingRepository;
    private final ShopRatingRepository shopRatingRepository;

    /**
     * Get admin dashboard overview
     */
    public AdminDashboardResponse getAdminDashboard() {
        Long totalShops = shopRepository.count();
        Long totalOwners = ownerRepository.count();
        Long totalWorkers = workerRepository.count();
        Long totalOrders = orderRepository.count();
        
        // Calculate active shops (shops with orders in last 30 days)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Order> recentOrders = orderRepository.findAll().stream()
                .filter(order -> order.getCreatedAt() != null && order.getCreatedAt().isAfter(thirtyDaysAgo))
                .collect(Collectors.toList());
        
        Long activeShops = recentOrders.stream()
                .map(order -> order.getShop().getShopId())
                .distinct()
                .count();
        
        // Calculate system growth (comparing last month vs previous month)
        Double systemGrowth = calculateSystemGrowth();
        
        return new AdminDashboardResponse(
            totalShops,
            totalOwners,
            totalWorkers,
            totalOrders,
            activeShops,
            systemGrowth
        );
    }

    /**
     * Get platform analytics
     */
    public PlatformAnalyticsResponse getPlatformAnalytics() {
        // System Metrics
        LocalDateTime today = LocalDate.now().atStartOfDay();
        LocalDateTime weekStart = LocalDate.now().minusDays(7).atStartOfDay();
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        
        List<Order> allOrders = orderRepository.findAll();
        
        Long ordersToday = allOrders.stream()
                .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().isAfter(today))
                .count();
        
        Long ordersThisWeek = allOrders.stream()
                .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().isAfter(weekStart))
                .count();
        
        Long ordersThisMonth = allOrders.stream()
                .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().isAfter(monthStart))
                .count();
        
        Long totalShops = shopRepository.count();
        Double averageOrdersPerShop = totalShops > 0 ? (double) allOrders.size() / totalShops : 0.0;
        
        Long totalWorkers = workerRepository.count();
        Double averageWorkersPerShop = totalShops > 0 ? (double) totalWorkers / totalShops : 0.0;
        
        PlatformAnalyticsResponse.SystemMetrics systemMetrics = new PlatformAnalyticsResponse.SystemMetrics(
            ordersToday,
            ordersThisWeek,
            ordersThisMonth,
            Math.round(averageOrdersPerShop * 10.0) / 10.0,
            Math.round(averageWorkersPerShop * 10.0) / 10.0
        );
        
        // Orders vs Shops Growth (last 7 months)
        List<PlatformAnalyticsResponse.MonthlyData> ordersVsShopsGrowth = getOrdersVsShopsGrowth();
        
        // Monthly Active Users (last 7 months)
        List<PlatformAnalyticsResponse.MonthlyActiveUsers> monthlyActiveUsers = getMonthlyActiveUsers();
        
        return new PlatformAnalyticsResponse(systemMetrics, ordersVsShopsGrowth, monthlyActiveUsers);
    }

    /**
     * Get shop analytics
     */
    public AdminAnalyticsResponse getShopAnalytics() {
        // Shop Status Distribution
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Order> recentOrders = orderRepository.findAll().stream()
                .filter(order -> order.getCreatedAt() != null && order.getCreatedAt().isAfter(thirtyDaysAgo))
                .collect(Collectors.toList());
        
        Long activeShops = recentOrders.stream()
                .map(order -> order.getShop().getShopId())
                .distinct()
                .count();
        
        Long totalShops = shopRepository.count();
        Long inactiveShops = totalShops - activeShops;
        
        AdminAnalyticsResponse.ShopStatusDistribution shopStatusDistribution = 
            new AdminAnalyticsResponse.ShopStatusDistribution(activeShops, inactiveShops);
        
        // Workers Distribution
        AdminAnalyticsResponse.WorkersDistribution workersDistribution = getWorkersDistribution();
        
        // Monthly Shop Registrations (last 7 months)
        List<AdminAnalyticsResponse.MonthlyShopRegistration> monthlyShopRegistrations = getMonthlyShopRegistrations();
        
        // Monthly Orders Processed (last 7 months)
        List<AdminAnalyticsResponse.MonthlyOrdersProcessed> monthlyOrdersProcessed = getMonthlyOrdersProcessed();
        
        return new AdminAnalyticsResponse(
            shopStatusDistribution,
            workersDistribution,
            monthlyShopRegistrations,
            monthlyOrdersProcessed
        );
    }

    /**
     * Get all shops with details
     */
    public List<AllShopsResponse> getAllShops(String searchQuery) {
        List<Shop> shops = shopRepository.findAll();
        
        return shops.stream()
                .filter(shop -> {
                    if (searchQuery == null || searchQuery.trim().isEmpty()) {
                        return true;
                    }
                    String query = searchQuery.toLowerCase();
                    return shop.getShopName().toLowerCase().contains(query) ||
                           (shop.getShopAddress() != null && shop.getShopAddress().toLowerCase().contains(query));
                })
                .map(this::mapToAllShopsResponse)
                .collect(Collectors.toList());
    }

    // Helper Methods

    private AllShopsResponse mapToAllShopsResponse(Shop shop) {
        // Get owner for this shop
        Owner owner = ownerRepository.findAll().stream()
                .filter(o -> o.getShop().getShopId().equals(shop.getShopId()))
                .findFirst()
                .orElse(null);
        
        String ownerName = owner != null && owner.getUser() != null ? owner.getUser().getName() : "N/A";
        String ownerEmail = owner != null && owner.getUser() != null ? owner.getUser().getEmail() : "N/A";
        String ownerContact = owner != null && owner.getUser() != null ? owner.getUser().getContactNumber() : "N/A";
        
        // Get total orders for this shop
        Long totalOrders = orderRepository.findAll().stream()
                .filter(order -> order.getShop().getShopId().equals(shop.getShopId()))
                .count();
        
        // Get total workers for this shop
        Long totalWorkers = (long) workerRepository.findByShop_ShopId(shop.getShopId()).size();
        
        // Check if shop is active (has orders in last 30 days)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        Boolean isActive = orderRepository.findAll().stream()
                .anyMatch(order -> order.getShop().getShopId().equals(shop.getShopId()) &&
                                 order.getCreatedAt() != null &&
                                 order.getCreatedAt().isAfter(thirtyDaysAgo));
        
        return new AllShopsResponse(
            shop.getShopId(),
            shop.getShopName(),
            ownerName,
            ownerEmail,
            ownerContact,
            totalOrders,
            totalWorkers,
            shop.getShopAddress(),
            null, // createdAt not available in Shop entity
            isActive
        );
    }

    private Double calculateSystemGrowth() {
        LocalDateTime lastMonthStart = LocalDate.now().minusMonths(1).withDayOfMonth(1).atStartOfDay();
        LocalDateTime lastMonthEnd = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime previousMonthStart = LocalDate.now().minusMonths(2).withDayOfMonth(1).atStartOfDay();
        
        List<Order> allOrders = orderRepository.findAll();
        
        long lastMonthOrders = allOrders.stream()
                .filter(o -> o.getCreatedAt() != null && 
                           o.getCreatedAt().isAfter(lastMonthStart) && 
                           o.getCreatedAt().isBefore(lastMonthEnd))
                .count();
        
        long previousMonthOrders = allOrders.stream()
                .filter(o -> o.getCreatedAt() != null && 
                           o.getCreatedAt().isAfter(previousMonthStart) && 
                           o.getCreatedAt().isBefore(lastMonthStart))
                .count();
        
        if (previousMonthOrders == 0) {
            return lastMonthOrders > 0 ? 100.0 : 0.0;
        }
        
        double growth = ((double) (lastMonthOrders - previousMonthOrders) / previousMonthOrders) * 100;
        return Math.round(growth * 10.0) / 10.0;
    }

    private List<PlatformAnalyticsResponse.MonthlyData> getOrdersVsShopsGrowth() {
        List<PlatformAnalyticsResponse.MonthlyData> result = new ArrayList<>();
        List<Order> allOrders = orderRepository.findAll();
        
        for (int i = 6; i >= 0; i--) {
            LocalDate monthDate = LocalDate.now().minusMonths(i);
            String monthName = monthDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            
            LocalDateTime monthStart = monthDate.withDayOfMonth(1).atStartOfDay();
            LocalDateTime monthEnd = monthDate.plusMonths(1).withDayOfMonth(1).atStartOfDay();
            
            long ordersCount = allOrders.stream()
                    .filter(o -> o.getCreatedAt() != null && 
                               o.getCreatedAt().isAfter(monthStart) && 
                               o.getCreatedAt().isBefore(monthEnd))
                    .count();
            
            long shopsCount = allOrders.stream()
                    .filter(o -> o.getCreatedAt() != null && 
                               o.getCreatedAt().isAfter(monthStart) && 
                               o.getCreatedAt().isBefore(monthEnd))
                    .map(o -> o.getShop().getShopId())
                    .distinct()
                    .count();
            
            result.add(new PlatformAnalyticsResponse.MonthlyData(monthName, ordersCount, shopsCount));
        }
        
        return result;
    }

    private List<PlatformAnalyticsResponse.MonthlyActiveUsers> getMonthlyActiveUsers() {
        List<PlatformAnalyticsResponse.MonthlyActiveUsers> result = new ArrayList<>();
        List<Order> allOrders = orderRepository.findAll();
        
        for (int i = 6; i >= 0; i--) {
            LocalDate monthDate = LocalDate.now().minusMonths(i);
            String monthName = monthDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            
            LocalDateTime monthStart = monthDate.withDayOfMonth(1).atStartOfDay();
            LocalDateTime monthEnd = monthDate.plusMonths(1).withDayOfMonth(1).atStartOfDay();
            
            // Count unique shop owners who had orders
            long ownersCount = allOrders.stream()
                    .filter(o -> o.getCreatedAt() != null && 
                               o.getCreatedAt().isAfter(monthStart) && 
                               o.getCreatedAt().isBefore(monthEnd))
                    .map(o -> o.getShop().getShopId())
                    .distinct()
                    .count();
            
            // Count unique workers who had tasks
            long workersCount = allOrders.stream()
                    .filter(o -> o.getCreatedAt() != null && 
                               o.getCreatedAt().isAfter(monthStart) && 
                               o.getCreatedAt().isBefore(monthEnd))
                    .flatMap(o -> workerRepository.findByShop_ShopId(o.getShop().getShopId()).stream())
                    .map(Worker::getId)
                    .distinct()
                    .count();
            
            result.add(new PlatformAnalyticsResponse.MonthlyActiveUsers(monthName, ownersCount, workersCount));
        }
        
        return result;
    }

    private AdminAnalyticsResponse.WorkersDistribution getWorkersDistribution() {
        List<Shop> allShops = shopRepository.findAll();
        
        long shops1to3 = 0;
        long shops4to6 = 0;
        long shops7to10 = 0;
        long shops10Plus = 0;
        
        for (Shop shop : allShops) {
            int workerCount = workerRepository.findByShop_ShopId(shop.getShopId()).size();
            
            if (workerCount >= 1 && workerCount <= 3) {
                shops1to3++;
            } else if (workerCount >= 4 && workerCount <= 6) {
                shops4to6++;
            } else if (workerCount >= 7 && workerCount <= 10) {
                shops7to10++;
            } else if (workerCount > 10) {
                shops10Plus++;
            }
        }
        
        return new AdminAnalyticsResponse.WorkersDistribution(shops1to3, shops4to6, shops7to10, shops10Plus);
    }

    private List<AdminAnalyticsResponse.MonthlyShopRegistration> getMonthlyShopRegistrations() {
        List<AdminAnalyticsResponse.MonthlyShopRegistration> result = new ArrayList<>();
        
        // Since Shop entity doesn't have createdAt, we'll use a placeholder
        // In a real scenario, you'd add createdAt to Shop entity
        for (int i = 6; i >= 0; i--) {
            LocalDate monthDate = LocalDate.now().minusMonths(i);
            String monthName = monthDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            
            // Placeholder: distribute shops evenly across months
            long shopsRegistered = shopRepository.count() / 7;
            
            result.add(new AdminAnalyticsResponse.MonthlyShopRegistration(monthName, shopsRegistered));
        }
        
        return result;
    }

    private List<AdminAnalyticsResponse.MonthlyOrdersProcessed> getMonthlyOrdersProcessed() {
        List<AdminAnalyticsResponse.MonthlyOrdersProcessed> result = new ArrayList<>();
        List<Order> allOrders = orderRepository.findAll();
        
        for (int i = 6; i >= 0; i--) {
            LocalDate monthDate = LocalDate.now().minusMonths(i);
            String monthName = monthDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            
            LocalDateTime monthStart = monthDate.withDayOfMonth(1).atStartOfDay();
            LocalDateTime monthEnd = monthDate.plusMonths(1).withDayOfMonth(1).atStartOfDay();
            
            long ordersProcessed = allOrders.stream()
                    .filter(o -> o.getCreatedAt() != null && 
                               o.getCreatedAt().isAfter(monthStart) && 
                               o.getCreatedAt().isBefore(monthEnd))
                    .count();
            
            result.add(new AdminAnalyticsResponse.MonthlyOrdersProcessed(monthName, ordersProcessed));
        }
        
        return result;
    }

    /**
     * Update shop details (admin only)
     */
    @Transactional
    public void updateShop(Long shopId, UpdateShopRequest request) {
        // Get shop
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));
        
        // Update shop details
        if (request.getShop() != null) {
            if (request.getShop().getShopName() != null) {
                shop.setShopName(request.getShop().getShopName());
            }
            if (request.getShop().getShopEmail() != null) {
                shop.setShopEmail(request.getShop().getShopEmail());
            }
            if (request.getShop().getShopMobileNo() != null) {
                shop.setShopMobileNo(request.getShop().getShopMobileNo());
            }
            if (request.getShop().getShopAddress() != null) {
                shop.setShopAddress(request.getShop().getShopAddress());
            }
        }
        shopRepository.save(shop);
        
        // Update owner details if provided
        if (request.getOwner() != null) {
            Owner owner = ownerRepository.findAll().stream()
                    .filter(o -> o.getShop().getShopId().equals(shopId))
                    .findFirst()
                    .orElse(null);
            
            if (owner != null && owner.getUser() != null) {
                User user = owner.getUser();
                if (request.getOwner().getName() != null) {
                    user.setName(request.getOwner().getName());
                }
                if (request.getOwner().getEmail() != null) {
                    user.setEmail(request.getOwner().getEmail());
                }
                if (request.getOwner().getContactNumber() != null) {
                    user.setContactNumber(request.getOwner().getContactNumber());
                }
                userRepository.save(user);
            }
        }
    }

    /**
     * Delete shop and all related data (admin only)
     * Cascade deletes: customers, workers, orders, tasks, measurements, ratings
     */
    @Transactional
    public void deleteShop(Long shopId) {
        // Verify shop exists
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));
        
        // Get all customers for this shop
        List<Customer> customers = customerRepository.findAll().stream()
                .filter(c -> c.getShop().getShopId().equals(shopId))
                .collect(Collectors.toList());
        
        // Delete measurement profiles for each customer
        for (Customer customer : customers) {
            measurementProfileRepository.deleteByCustomer_Id(customer.getId());
        }
        
        // Get all workers for this shop
        List<Worker> workers = workerRepository.findByShop_ShopId(shopId);
        
        // Get all orders for this shop
        List<Order> orders = orderRepository.findAll().stream()
                .filter(o -> o.getShop().getShopId().equals(shopId))
                .collect(Collectors.toList());
        
        // Delete order-related data
        for (Order order : orders) {
            // Delete tasks for this order
            List<Task> tasks = taskRepository.findByOrder_OrderId(order.getOrderId());
            taskRepository.deleteAll(tasks);
            
            // Delete order items
            List<OrderItem> orderItems = orderItemRepository.findByOrder_OrderId(order.getOrderId());
            orderItemRepository.deleteAll(orderItems);
            
            // Delete order activities
            List<OrderActivity> activities = orderActivityRepository.findAll().stream()
                    .filter(a -> a.getOrder().getOrderId().equals(order.getOrderId()))
                    .collect(Collectors.toList());
            orderActivityRepository.deleteAll(activities);
        }
        
        // Delete orders
        orderRepository.deleteAll(orders);
        
        // Delete workers and their related data
        for (Worker worker : workers) {
            // Delete rates for this worker
            rateRepository.deleteAll(rateRepository.findByWorker_Id(worker.getId()));
            
            // Delete worker ratings
            workerRatingRepository.deleteAll(
                workerRatingRepository.findAll().stream()
                    .filter(wr -> wr.getWorker().getId().equals(worker.getId()))
                    .collect(Collectors.toList())
            );
            
            // Delete worker
            workerRepository.delete(worker);
            
            // Delete worker's user account
            if (worker.getUser() != null) {
                userRepository.delete(worker.getUser());
            }
        }
        
        // Delete customers and their user accounts
        for (Customer customer : customers) {
            customerRepository.delete(customer);
            
            // Delete customer's user account
            if (customer.getUser() != null) {
                userRepository.delete(customer.getUser());
            }
        }
        
        // Delete shop ratings
        shopRatingRepository.deleteAll(
            shopRatingRepository.findAll().stream()
                .filter(sr -> sr.getShop().getShopId().equals(shopId))
                .collect(Collectors.toList())
        );
        
        // Get owner for this shop
        Owner owner = ownerRepository.findAll().stream()
                .filter(o -> o.getShop().getShopId().equals(shopId))
                .findFirst()
                .orElse(null);
        
        // Delete owner
        if (owner != null) {
            ownerRepository.delete(owner);
            
            // Delete owner's user account
            if (owner.getUser() != null) {
                userRepository.delete(owner.getUser());
            }
        }
        
        // Finally, delete the shop
        shopRepository.delete(shop);
    }
}