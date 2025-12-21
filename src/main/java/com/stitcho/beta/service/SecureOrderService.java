package com.stitcho.beta.service;

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
import com.stitcho.beta.dto.OrderResponse;
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

    public List<OrderResponse> getMyOrders(Long userId, String role) {
        List<Order> orders;

        if ("OWNER".equalsIgnoreCase(role)) {
            Owner owner = ownerRepository.findByUser_Id(userId)
                    .orElseThrow(() -> new RuntimeException("Owner not found"));
            orders = orderRepository.findByShop_ShopId(owner.getShop().getShopId());
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
}
