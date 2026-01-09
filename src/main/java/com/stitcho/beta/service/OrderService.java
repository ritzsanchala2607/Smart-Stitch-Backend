package com.stitcho.beta.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stitcho.beta.Repository.CustomerRepository;
import com.stitcho.beta.Repository.OrderItemRepository;
import com.stitcho.beta.Repository.OrderRepository;
import com.stitcho.beta.Repository.ShopRepository;
import com.stitcho.beta.Repository.TaskRepository;
import com.stitcho.beta.Repository.WorkerRepository;
import com.stitcho.beta.dto.CreateOrderRequest;
import com.stitcho.beta.dto.OrderResponse;
import com.stitcho.beta.entity.Customer;
import com.stitcho.beta.entity.Order;
import com.stitcho.beta.entity.OrderItem;
import com.stitcho.beta.entity.OrderStatus;
import com.stitcho.beta.entity.Shop;
import com.stitcho.beta.entity.Task;
import com.stitcho.beta.entity.TaskStatus;
import com.stitcho.beta.entity.TaskType;
import com.stitcho.beta.entity.Worker;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final TaskRepository taskRepository;
    private final CustomerRepository customerRepository;
    private final WorkerRepository workerRepository;
    private final ShopRepository shopRepository;
    private final ActivityLogService activityLogService;

    @Transactional
    public Long createOrder(Long shopId, CreateOrderRequest request) {
        // 1️⃣ Validate shop
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        // 2️⃣ Validate customer belongs to shop
        Customer customer = customerRepository.findByIdAndShop_ShopId(request.getCustomerId(), shopId)
                .orElseThrow(() -> new RuntimeException("Customer not found in this shop"));

        // 3️⃣ Create order
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

        // 4️⃣ Create order items
        for (CreateOrderRequest.OrderItem itemReq : request.getItems()) {
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setItemName(itemReq.getItemName());
            item.setQuantity(itemReq.getQuantity());
            item.setPrice(itemReq.getPrice());
            item.setFabricType(itemReq.getFabricType());
            orderItemRepository.save(item);
        }

        // 5️⃣ Create tasks
        for (CreateOrderRequest.TaskRequest taskReq : request.getTasks()) {
            Worker worker = workerRepository.findById(taskReq.getWorkerId())
                    .orElseThrow(() -> new RuntimeException("Worker not found"));

            // Validate worker belongs to shop
            if (!worker.getShop().getShopId().equals(shopId)) {
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

    public OrderResponse getOrder(Long shopId, Long orderId) {
        Order order = orderRepository.findByOrderIdAndShop_ShopId(orderId, shopId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        return mapToOrderResponse(order);
    }

    public List<OrderResponse> getCustomerOrders(Long shopId, Long customerId) {
        List<Order> orders = orderRepository.findByCustomer_IdAndShop_ShopId(customerId, shopId);
        return orders.stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    public List<OrderResponse> getShopOrders(Long shopId) {
        // Validate shop exists
        shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));
        
        List<Order> orders = orderRepository.findByShop_ShopId(shopId);
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

        // Customer info
        if (order.getCustomer() != null && order.getCustomer().getUser() != null) {
            Customer customer = order.getCustomer();
            OrderResponse.CustomerInfo customerInfo = new OrderResponse.CustomerInfo();
            customerInfo.setCustomerId(customer.getId());
            customerInfo.setName(customer.getUser().getName());
            customerInfo.setEmail(customer.getUser().getEmail());
            customerInfo.setContactNumber(customer.getUser().getContactNumber());
            response.setCustomer(customerInfo);
        }

        // Order items
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

        // Tasks
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

    @Transactional
    public void updateOrderStatus(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        OrderStatus oldStatus = order.getStatus();
        List<Task> allTasks = taskRepository.findByOrder_OrderId(orderId);
        
        // Check if all tasks are completed
        boolean allCompleted = allTasks.stream()
                .allMatch(task -> task.getStatus() == TaskStatus.COMPLETED);

        if (allCompleted) {
            order.setStatus(OrderStatus.COMPLETED);
        } else {
            // Find highest priority in-progress task
            boolean hasCuttingInProgress = allTasks.stream()
                    .anyMatch(t -> t.getTaskType() == TaskType.CUTTING && t.getStatus() == TaskStatus.IN_PROGRESS);
            boolean hasStitchingInProgress = allTasks.stream()
                    .anyMatch(t -> t.getTaskType() == TaskType.STITCHING && t.getStatus() == TaskStatus.IN_PROGRESS);
            boolean hasIroningInProgress = allTasks.stream()
                    .anyMatch(t -> t.getTaskType() == TaskType.IRONING && t.getStatus() == TaskStatus.IN_PROGRESS);

            if (hasCuttingInProgress) {
                order.setStatus(OrderStatus.CUTTING);
            } else if (hasStitchingInProgress) {
                order.setStatus(OrderStatus.STITCHING);
            } else if (hasIroningInProgress) {
                order.setStatus(OrderStatus.IRONING);
            }
        }

        orderRepository.save(order);

        // Log status change if status actually changed
        if (oldStatus != order.getStatus()) {
            activityLogService.logStatusChange(order, oldStatus, order.getStatus());
        }
    }
}
