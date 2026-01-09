package com.stitcho.beta.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stitcho.beta.Repository.CustomerRepository;
import com.stitcho.beta.Repository.OrderItemRepository;
import com.stitcho.beta.Repository.OrderRepository;
import com.stitcho.beta.Repository.OwnerRepository;
import com.stitcho.beta.Repository.TaskRepository;
import com.stitcho.beta.Repository.WorkerRepository;
import com.stitcho.beta.dto.CreateOrderRequest;
import com.stitcho.beta.dto.DailyOrderSummary;
import com.stitcho.beta.dto.OrderResponse;
import com.stitcho.beta.dto.OrderStatusResponse;
import com.stitcho.beta.dto.UpdateOrderRequest;
import com.stitcho.beta.dto.WeeklyOrderSummary;
import com.stitcho.beta.entity.Customer;
import com.stitcho.beta.entity.Order;
import com.stitcho.beta.entity.OrderItem;
import com.stitcho.beta.entity.OrderStatus;
import com.stitcho.beta.entity.Owner;
import com.stitcho.beta.entity.Shop;
import com.stitcho.beta.entity.Task;
import com.stitcho.beta.entity.TaskStatus;
import com.stitcho.beta.entity.TaskType;
import com.stitcho.beta.entity.Worker;
import com.stitcho.beta.entity.Worker;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SecureOrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final TaskRepository taskRepository;
    private final CustomerRepository customerRepository;
    private final WorkerRepository workerRepository;
    private final OwnerRepository ownerRepository;

    @Transactional
    public Long createOrder(Long userId, CreateOrderRequest request) {
        // Get owner's shop
        Owner owner = ownerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));
        
        Shop shop = owner.getShop();

        // Validate customer belongs to shop
        Customer customer = customerRepository.findByIdAndShop_ShopId(request.getCustomerId(), shop.getShopId())
                .orElseThrow(() -> new RuntimeException("Customer not found in this shop"));

        // Create order
        Order order = new Order();
        order.setCustomer(customer);
        order.setShop(shop);
        order.setDeadline(request.getDeadline());
        order.setTotalPrice(request.getTotalPrice());
        order.setPaidAmount(request.getAdvancePayment());
        order.setPaymentStatus("PAID");
        order.setNotes(request.getAdditionalNotes());
        order.setStatus(OrderStatus.NEW);
        order = orderRepository.save(order);

        // Create order items
        for (CreateOrderRequest.OrderItem itemReq : request.getItems()) {
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setItemName(itemReq.getItemName());
            item.setQuantity(itemReq.getQuantity());
            item.setPrice(itemReq.getPrice());
            item.setFabricType(itemReq.getFabricType());
            orderItemRepository.save(item);
        }

        // Create tasks
        for (CreateOrderRequest.TaskRequest taskReq : request.getTasks()) {
            Worker worker = workerRepository.findById(taskReq.getWorkerId())
                    .orElseThrow(() -> new RuntimeException("Worker not found"));

            if (!worker.getShop().getShopId().equals(shop.getShopId())) {
                throw new RuntimeException("Worker does not belong to this shop");
            }

            Task task = new Task();
            task.setOrder(order);
            task.setWorker(worker);
            task.setTaskType(TaskType.valueOf(taskReq.getTaskType().toUpperCase()));
            task.setStatus(TaskStatus.PENDING);
            task.setAssignedAt(LocalDateTime.now());
            taskRepository.save(task);
        }

        return order.getOrderId();
    }

    public OrderResponse getOrder(Long userId, String role, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Access control
        if ("OWNER".equalsIgnoreCase(role)) {
            Owner owner = ownerRepository.findByUser_Id(userId)
                    .orElseThrow(() -> new RuntimeException("Owner not found"));
            if (!order.getShop().getShopId().equals(owner.getShop().getShopId())) {
                throw new RuntimeException("Access denied");
            }
        } else if ("CUSTOMER".equalsIgnoreCase(role)) {
            if (order.getCustomer().getUser().getId() != userId) {
                throw new RuntimeException("Access denied");
            }
        } else if ("WORKER".equalsIgnoreCase(role)) {
            Worker worker = workerRepository.findByUser_Id(userId)
                    .orElseThrow(() -> new RuntimeException("Worker not found"));
            boolean hasTask = taskRepository.findByOrder_OrderId(orderId).stream()
                    .anyMatch(t -> t.getWorker().getId().equals(worker.getId()));
            if (!hasTask) {
                throw new RuntimeException("Access denied");
            }
        }

        return mapToOrderResponse(order);
    }

    public List<OrderResponse> getMyOrders(Long userId, String role, String customerName) {
        List<Order> orders;

        if ("OWNER".equalsIgnoreCase(role)) {
            Owner owner = ownerRepository.findByUser_Id(userId)
                    .orElseThrow(() -> new RuntimeException("Owner not found"));
            
            // If customer name is provided, search by name
            if (customerName != null && !customerName.trim().isEmpty()) {
                orders = orderRepository.findByShop_ShopIdAndCustomer_User_NameContainingIgnoreCase(
                    owner.getShop().getShopId(), 
                    customerName
                );
            } else {
                orders = orderRepository.findByShop_ShopId(owner.getShop().getShopId());
            }
        } else if ("CUSTOMER".equalsIgnoreCase(role)) {
            // Find customer by userId
            Customer customer = customerRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
            orders = orderRepository.findByCustomer_IdAndShop_ShopId(customer.getId(), customer.getShop().getShopId());
        } else {
            throw new RuntimeException("Invalid role for this operation");
        }

        return orders.stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    private OrderResponse mapToOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setOrderId(order.getOrderId());
        response.setStatus(order.getStatus() != null ? order.getStatus().name() : "NEW");
        response.setDeadline(order.getDeadline());
        response.setTotalPrice(order.getTotalPrice());
        response.setPaidAmount(order.getPaidAmount());
        response.setPaymentStatus(order.getPaymentStatus());
        response.setNotes(order.getNotes());
        response.setCreatedAt(order.getCreatedAt());

        if (order.getCustomer() != null && order.getCustomer().getUser() != null) {
            Customer customer = order.getCustomer();
            OrderResponse.CustomerInfo customerInfo = new OrderResponse.CustomerInfo();
            customerInfo.setCustomerId(customer.getId());
            customerInfo.setName(customer.getUser().getName());
            customerInfo.setEmail(customer.getUser().getEmail());
            customerInfo.setContactNumber(customer.getUser().getContactNumber());
            response.setCustomer(customerInfo);
        }

        List<OrderItem> items = orderItemRepository.findByOrder_OrderId(order.getOrderId());
        List<OrderResponse.OrderItemInfo> itemInfos = items.stream()
                .map(item -> {
                    OrderResponse.OrderItemInfo info = new OrderResponse.OrderItemInfo();
                    info.setItemId(item.getItemId());
                    info.setItemName(item.getItemName());
                    info.setQuantity(item.getQuantity());
                    info.setPrice(item.getPrice());
                    info.setFabricType(item.getFabricType());
                    return info;
                })
                .collect(Collectors.toList());
        response.setItems(itemInfos);

        List<Task> tasks = taskRepository.findByOrder_OrderId(order.getOrderId());
        List<OrderResponse.TaskInfo> taskInfos = tasks.stream()
                .map(task -> {
                    OrderResponse.TaskInfo info = new OrderResponse.TaskInfo();
                    info.setTaskId(task.getTaskId());
                    info.setTaskType(task.getTaskType() != null ? task.getTaskType().name() : null);
                    info.setStatus(task.getStatus() != null ? task.getStatus().name() : "PENDING");
                    if (task.getWorker() != null) {
                        info.setWorkerId(task.getWorker().getId());
                        if (task.getWorker().getUser() != null) {
                            info.setWorkerName(task.getWorker().getUser().getName());
                        }
                    }
                    info.setStartedAt(task.getStartedAt());
                    info.setCompletedAt(task.getCompletedAt());
                    return info;
                })
                .collect(Collectors.toList());
        response.setTasks(taskInfos);

        return response;
    }

    public DailyOrderSummary getDailyOrders(Long userId, LocalDate date) {
        // Get owner's shop
        Owner owner = ownerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));
        
        Long shopId = owner.getShop().getShopId();

        // Get orders created on the specified date
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
        
        List<Order> orders = orderRepository.findByShop_ShopIdAndCreatedAtBetween(shopId, startOfDay, endOfDay);

        // Map to summary
        List<DailyOrderSummary.OrderSummary> orderSummaries = orders.stream()
                .map(this::mapToOrderSummary)
                .collect(Collectors.toList());

        DailyOrderSummary summary = new DailyOrderSummary();
        summary.setTotalOrders(orders.size());
        summary.setOrders(orderSummaries);

        return summary;
    }

    private DailyOrderSummary.OrderSummary mapToOrderSummary(Order order) {
        DailyOrderSummary.OrderSummary summary = new DailyOrderSummary.OrderSummary();
        summary.setOrderId(order.getOrderId());
        summary.setCustomerName(order.getCustomer() != null && order.getCustomer().getUser() != null 
                ? order.getCustomer().getUser().getName() 
                : "Unknown");
        summary.setTotalAmount(order.getTotalPrice());
        summary.setStatus(order.getStatus() != null ? order.getStatus().name() : "NEW");
        summary.setDeliveryDate(order.getDeadline());
        summary.setCreatedAt(order.getCreatedAt());

        // Get items
        List<OrderItem> items = orderItemRepository.findByOrder_OrderId(order.getOrderId());
        List<String> itemNames = items.stream()
                .map(item -> item.getItemName() + " (x" + item.getQuantity() + ")")
                .collect(Collectors.toList());
        summary.setItems(itemNames);

        // Get workers and tasks
        List<Task> tasks = taskRepository.findByOrder_OrderId(order.getOrderId());
        List<DailyOrderSummary.WorkerInfo> workerInfos = tasks.stream()
                .map(task -> {
                    DailyOrderSummary.WorkerInfo workerInfo = new DailyOrderSummary.WorkerInfo();
                    if (task.getWorker() != null) {
                        workerInfo.setWorkerId(task.getWorker().getId());
                        workerInfo.setWorkerName(task.getWorker().getUser() != null 
                                ? task.getWorker().getUser().getName() 
                                : "Unknown");
                    }
                    workerInfo.setTaskType(task.getTaskType() != null ? task.getTaskType().name() : null);
                    workerInfo.setTaskStatus(task.getStatus() != null ? task.getStatus().name() : "PENDING");
                    return workerInfo;
                })
                .collect(Collectors.toList());
        summary.setWorkers(workerInfos);

        return summary;
    }

    public WeeklyOrderSummary getWeeklyOrders(Long userId) {
        // Get owner's shop
        Owner owner = ownerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));
        
        Long shopId = owner.getShop().getShopId();

        // Get orders from last 7 days (including today)
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6); // Last 7 days including today
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
        
        List<Order> orders = orderRepository.findByShop_ShopIdAndCreatedAtBetween(shopId, startDateTime, endDateTime);

        // Map to summary
        List<WeeklyOrderSummary.OrderSummary> orderSummaries = orders.stream()
                .map(this::mapToWeeklyOrderSummary)
                .collect(Collectors.toList());

        WeeklyOrderSummary summary = new WeeklyOrderSummary();
        summary.setTotalOrders(orders.size());
        summary.setStartDate(startDate);
        summary.setEndDate(endDate);
        summary.setOrders(orderSummaries);

        return summary;
    }

    private WeeklyOrderSummary.OrderSummary mapToWeeklyOrderSummary(Order order) {
        WeeklyOrderSummary.OrderSummary summary = new WeeklyOrderSummary.OrderSummary();
        summary.setOrderId(order.getOrderId());
        summary.setCustomerName(order.getCustomer() != null && order.getCustomer().getUser() != null 
                ? order.getCustomer().getUser().getName() 
                : "Unknown");
        summary.setTotalAmount(order.getTotalPrice());
        summary.setStatus(order.getStatus() != null ? order.getStatus().name() : "NEW");
        summary.setDeliveryDate(order.getDeadline());
        summary.setCreatedAt(order.getCreatedAt());

        // Get items
        List<OrderItem> items = orderItemRepository.findByOrder_OrderId(order.getOrderId());
        List<String> itemNames = items.stream()
                .map(item -> item.getItemName() + " (x" + item.getQuantity() + ")")
                .collect(Collectors.toList());
        summary.setItems(itemNames);

        // Get workers and tasks
        List<Task> tasks = taskRepository.findByOrder_OrderId(order.getOrderId());
        List<WeeklyOrderSummary.WorkerInfo> workerInfos = tasks.stream()
                .map(task -> {
                    WeeklyOrderSummary.WorkerInfo workerInfo = new WeeklyOrderSummary.WorkerInfo();
                    if (task.getWorker() != null) {
                        workerInfo.setWorkerId(task.getWorker().getId());
                        workerInfo.setWorkerName(task.getWorker().getUser() != null 
                                ? task.getWorker().getUser().getName() 
                                : "Unknown");
                    }
                    workerInfo.setTaskType(task.getTaskType() != null ? task.getTaskType().name() : null);
                    workerInfo.setTaskStatus(task.getStatus() != null ? task.getStatus().name() : "PENDING");
                    return workerInfo;
                })
                .collect(Collectors.toList());
        summary.setWorkers(workerInfos);

        return summary;
    }

    @Transactional
    public void updateOrder(Long userId, String role, Long orderId, UpdateOrderRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Access control - only owner can update orders
        if ("OWNER".equalsIgnoreCase(role)) {
            Owner owner = ownerRepository.findByUser_Id(userId)
                    .orElseThrow(() -> new RuntimeException("Owner not found"));
            if (!order.getShop().getShopId().equals(owner.getShop().getShopId())) {
                throw new RuntimeException("Access denied");
            }
        } else {
            throw new RuntimeException("Only owners can update orders");
        }

        // Update order fields
        if (request.getDeadline() != null) {
            order.setDeadline(request.getDeadline());
        }
        if (request.getTotalPrice() != null) {
            order.setTotalPrice(request.getTotalPrice());
        }
        if (request.getPaidAmount() != null) {
            order.setPaidAmount(request.getPaidAmount());
        }
        if (request.getPaymentStatus() != null) {
            order.setPaymentStatus(request.getPaymentStatus());
        }
        if (request.getNotes() != null) {
            order.setNotes(request.getNotes());
        }

        orderRepository.save(order);
    }

    @Transactional
    public void deleteOrder(Long userId, String role, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Access control - only owner can delete orders
        if ("OWNER".equalsIgnoreCase(role)) {
            Owner owner = ownerRepository.findByUser_Id(userId)
                    .orElseThrow(() -> new RuntimeException("Owner not found"));
            if (!order.getShop().getShopId().equals(owner.getShop().getShopId())) {
                throw new RuntimeException("Access denied");
            }
        } else {
            throw new RuntimeException("Only owners can delete orders");
        }

        // Delete associated tasks first (due to foreign key constraints)
        List<Task> tasks = taskRepository.findByOrder_OrderId(orderId);
        taskRepository.deleteAll(tasks);

        // Delete associated order items
        List<OrderItem> items = orderItemRepository.findByOrder_OrderId(orderId);
        orderItemRepository.deleteAll(items);

        // Delete the order
        orderRepository.delete(order);
    }

    public List<OrderResponse> getOrdersByUserId(Long userId, String role) {
        List<Order> orders;

        if ("CUSTOMER".equalsIgnoreCase(role)) {
            // Find customer by userId
            Customer customer = customerRepository.findByUser_Id(userId)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
            orders = orderRepository.findByCustomer_Id(customer.getId());
        } else if ("OWNER".equalsIgnoreCase(role)) {
            // Owner gets all orders from their shop
            Owner owner = ownerRepository.findByUser_Id(userId)
                    .orElseThrow(() -> new RuntimeException("Owner not found"));
            orders = orderRepository.findByShop_ShopId(owner.getShop().getShopId());
        } else if ("WORKER".equalsIgnoreCase(role)) {
            // Worker gets orders where they have tasks
            Worker worker = workerRepository.findByUser_Id(userId)
                    .orElseThrow(() -> new RuntimeException("Worker not found"));
            
            // Get all tasks for this worker
            List<Task> workerTasks = taskRepository.findByWorker_Id(worker.getId());
            
            // Extract unique order IDs
            List<Long> orderIds = workerTasks.stream()
                    .map(task -> task.getOrder().getOrderId())
                    .distinct()
                    .collect(Collectors.toList());
            
            // Fetch orders
            orders = orderRepository.findAllById(orderIds);
        } else {
            throw new RuntimeException("Invalid role");
        }

        return orders.stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    public List<OrderStatusResponse> getOrdersStatus(Long userId, String role) {
        List<Order> orders;

        if ("CUSTOMER".equalsIgnoreCase(role)) {
            Customer customer = customerRepository.findByUser_Id(userId)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
            orders = orderRepository.findByCustomer_Id(customer.getId());
        } else if ("OWNER".equalsIgnoreCase(role)) {
            Owner owner = ownerRepository.findByUser_Id(userId)
                    .orElseThrow(() -> new RuntimeException("Owner not found"));
            orders = orderRepository.findByShop_ShopId(owner.getShop().getShopId());
        } else if ("WORKER".equalsIgnoreCase(role)) {
            Worker worker = workerRepository.findByUser_Id(userId)
                    .orElseThrow(() -> new RuntimeException("Worker not found"));
            List<Task> workerTasks = taskRepository.findByWorker_Id(worker.getId());
            List<Long> orderIds = workerTasks.stream()
                    .map(task -> task.getOrder().getOrderId())
                    .distinct()
                    .collect(Collectors.toList());
            orders = orderRepository.findAllById(orderIds);
        } else {
            throw new RuntimeException("Invalid role");
        }

        return orders.stream()
                .map(this::mapToOrderStatusResponse)
                .collect(Collectors.toList());
    }

    private OrderStatusResponse mapToOrderStatusResponse(Order order) {
        OrderStatusResponse response = new OrderStatusResponse();
        response.setOrderId(order.getOrderId());
        response.setOrderStatus(order.getStatus() != null ? order.getStatus().name() : "NEW");
        response.setDeadline(order.getDeadline());
        response.setCreatedAt(order.getCreatedAt());
        
        if (order.getCustomer() != null && order.getCustomer().getUser() != null) {
            response.setCustomerName(order.getCustomer().getUser().getName());
        }

        // Get tasks and create combined status strings
        List<Task> tasks = taskRepository.findByOrder_OrderId(order.getOrderId());
        List<String> taskStatuses = tasks.stream()
                .map(task -> {
                    String taskType = task.getTaskType() != null ? task.getTaskType().name() : "UNKNOWN";
                    String status = task.getStatus() != null ? task.getStatus().name() : "PENDING";
                    return taskType + "_" + status;
                })
                .collect(Collectors.toList());
        
        response.setTaskStatuses(taskStatuses);

        return response;
    }

    @Transactional
    public void deliverOrder(Long userId, String role, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Access control - only owner can mark orders as delivered
        if ("OWNER".equalsIgnoreCase(role)) {
            Owner owner = ownerRepository.findByUser_Id(userId)
                    .orElseThrow(() -> new RuntimeException("Owner not found"));
            if (!order.getShop().getShopId().equals(owner.getShop().getShopId())) {
                throw new RuntimeException("Access denied");
            }
        } else {
            throw new RuntimeException("Only owners can mark orders as delivered");
        }

        // Validate order is in COMPLETED status
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new RuntimeException("Order must be in COMPLETED status before marking as delivered");
        }

        // Update status to DELIVERED
        order.setStatus(OrderStatus.DELIVERED);
        orderRepository.save(order);
    }

    public List<com.stitcho.beta.dto.CustomerOrderDetailResponse> getCustomerOrdersWithDetails(Long customerId) {
        // Get customer
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Get all orders for this customer
        List<Order> orders = orderRepository.findByCustomer_Id(customerId);

        return orders.stream()
                .map(this::mapToCustomerOrderDetailResponse)
                .collect(Collectors.toList());
    }

    private com.stitcho.beta.dto.CustomerOrderDetailResponse mapToCustomerOrderDetailResponse(Order order) {
        com.stitcho.beta.dto.CustomerOrderDetailResponse response = new com.stitcho.beta.dto.CustomerOrderDetailResponse();
        
        // Order basic info
        response.setOrderId(order.getOrderId());
        response.setOrderStatus(order.getStatus() != null ? order.getStatus().name() : "NEW");
        response.setDeadline(order.getDeadline());
        response.setTotalPrice(order.getTotalPrice());
        response.setPaidAmount(order.getPaidAmount());
        response.setPaymentStatus(order.getPaymentStatus());
        response.setNotes(order.getNotes());
        response.setCreatedAt(order.getCreatedAt());

        // Shop information
        Shop shop = order.getShop();
        if (shop != null) {
            com.stitcho.beta.dto.CustomerOrderDetailResponse.ShopInfo shopInfo = 
                new com.stitcho.beta.dto.CustomerOrderDetailResponse.ShopInfo();
            shopInfo.setShopId(shop.getShopId());
            shopInfo.setShopName(shop.getShopName());
            shopInfo.setShopEmail(shop.getShopEmail());
            shopInfo.setShopContactNumber(shop.getShopMobileNo());
            shopInfo.setShopAddress(shop.getShopAddress());
            response.setShop(shopInfo);
        }

        // Order items
        List<OrderItem> items = orderItemRepository.findByOrder_OrderId(order.getOrderId());
        List<com.stitcho.beta.dto.CustomerOrderDetailResponse.OrderItemInfo> itemInfos = items.stream()
                .map(item -> {
                    com.stitcho.beta.dto.CustomerOrderDetailResponse.OrderItemInfo itemInfo = 
                        new com.stitcho.beta.dto.CustomerOrderDetailResponse.OrderItemInfo();
                    itemInfo.setItemId(item.getItemId());
                    itemInfo.setItemName(item.getItemName());
                    itemInfo.setQuantity(item.getQuantity());
                    itemInfo.setPrice(item.getPrice());
                    itemInfo.setFabricType(item.getFabricType());
                    return itemInfo;
                })
                .collect(Collectors.toList());
        response.setItems(itemInfos);

        // Tasks with worker information
        List<Task> tasks = taskRepository.findByOrder_OrderId(order.getOrderId());
        List<com.stitcho.beta.dto.CustomerOrderDetailResponse.TaskWithWorkerInfo> taskInfos = tasks.stream()
                .map(task -> {
                    com.stitcho.beta.dto.CustomerOrderDetailResponse.TaskWithWorkerInfo taskInfo = 
                        new com.stitcho.beta.dto.CustomerOrderDetailResponse.TaskWithWorkerInfo();
                    taskInfo.setTaskId(task.getTaskId());
                    taskInfo.setTaskType(task.getTaskType() != null ? task.getTaskType().name() : "UNKNOWN");
                    taskInfo.setTaskStatus(task.getStatus() != null ? task.getStatus().name() : "PENDING");
                    taskInfo.setAssignedAt(task.getAssignedAt());
                    taskInfo.setStartedAt(task.getStartedAt());
                    taskInfo.setCompletedAt(task.getCompletedAt());
                    
                    // Worker information
                    Worker worker = task.getWorker();
                    if (worker != null) {
                        taskInfo.setWorkerId(worker.getId());
                        if (worker.getUser() != null) {
                            taskInfo.setWorkerName(worker.getUser().getName());
                            taskInfo.setWorkerContactNumber(worker.getUser().getContactNumber());
                        }
                    }
                    
                    return taskInfo;
                })
                .collect(Collectors.toList());
        response.setTasks(taskInfos);

        return response;
    }
}
