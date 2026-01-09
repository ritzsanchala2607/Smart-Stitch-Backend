package com.stitcho.beta.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stitcho.beta.Repository.CustomerRepository;
import com.stitcho.beta.Repository.OrderRepository;
import com.stitcho.beta.Repository.ShopRatingRepository;
import com.stitcho.beta.Repository.TaskRepository;
import com.stitcho.beta.Repository.WorkerRatingRepository;
import com.stitcho.beta.Repository.WorkerRepository;
import com.stitcho.beta.dto.OrderForRatingResponse;
import com.stitcho.beta.dto.RatingResponse;
import com.stitcho.beta.dto.RatingSummary;
import com.stitcho.beta.dto.ShopRatingRequest;
import com.stitcho.beta.dto.WorkerRatingRequest;
import com.stitcho.beta.entity.Customer;
import com.stitcho.beta.entity.Order;
import com.stitcho.beta.entity.OrderStatus;
import com.stitcho.beta.entity.ShopRating;
import com.stitcho.beta.entity.Task;
import com.stitcho.beta.entity.Worker;
import com.stitcho.beta.entity.WorkerRating;

@Service
public class RatingService {

    @Autowired
    private ShopRatingRepository shopRatingRepository;

    @Autowired
    private WorkerRatingRepository workerRatingRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private WorkerRepository workerRepository;

    @Autowired
    private TaskRepository taskRepository;

    // ==================== SHOP RATING METHODS ====================

    @Transactional
    public RatingResponse rateShop(Long customerId, ShopRatingRequest request) {
        // Validate customer
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Validate order
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Verify order belongs to customer
        if (!order.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("Order does not belong to this customer");
        }

        // Verify order is completed
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new RuntimeException("Can only rate completed orders");
        }

        // Check if rating already exists
        if (shopRatingRepository.existsByShop_ShopIdAndOrder_OrderId(
                order.getShop().getShopId(), request.getOrderId())) {
            throw new RuntimeException("You have already rated this shop for this order");
        }

        // Create rating
        ShopRating rating = new ShopRating();
        rating.setShop(order.getShop());
        rating.setCustomer(customer);
        rating.setOrder(order);
        rating.setRating(request.getRating());
        rating.setReview(request.getReview());

        ShopRating savedRating = shopRatingRepository.save(rating);

        return mapToRatingResponse(savedRating);
    }

    public List<RatingResponse> getShopRatings(Long shopId) {
        List<ShopRating> ratings = shopRatingRepository.findByShop_ShopId(shopId);
        return ratings.stream()
                .map(this::mapToRatingResponse)
                .collect(Collectors.toList());
    }

    public RatingSummary getShopRatingSummary(Long shopId) {
        Double avgRating = shopRatingRepository.getAverageRatingByShopId(shopId);
        Long totalRatings = shopRatingRepository.getRatingCountByShopId(shopId);

        return new RatingSummary(
                avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0,
                totalRatings != null ? totalRatings : 0L
        );
    }

    // ==================== WORKER RATING METHODS ====================

    @Transactional
    public RatingResponse rateWorker(Long customerId, WorkerRatingRequest request) {
        // Validate customer
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Validate order
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Verify order belongs to customer
        if (!order.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("Order does not belong to this customer");
        }

        // Verify order is completed
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new RuntimeException("Can only rate completed orders");
        }

        // Validate worker
        Worker worker = workerRepository.findById(request.getWorkerId())
                .orElseThrow(() -> new RuntimeException("Worker not found"));

        // Verify worker belongs to same shop as order
        if (!worker.getShop().getShopId().equals(order.getShop().getShopId())) {
            throw new RuntimeException("Worker does not belong to this shop");
        }

        // Check if rating already exists
        if (workerRatingRepository.existsByWorker_IdAndOrder_OrderId(
                request.getWorkerId(), request.getOrderId())) {
            throw new RuntimeException("You have already rated this worker for this order");
        }

        // Create rating
        WorkerRating rating = new WorkerRating();
        rating.setWorker(worker);
        rating.setCustomer(customer);
        rating.setOrder(order);
        rating.setRating(request.getRating());
        rating.setReview(request.getReview());

        WorkerRating savedRating = workerRatingRepository.save(rating);

        return mapToRatingResponse(savedRating);
    }

    public List<RatingResponse> getWorkerRatings(Long workerId) {
        List<WorkerRating> ratings = workerRatingRepository.findByWorker_Id(workerId);
        return ratings.stream()
                .map(this::mapToRatingResponse)
                .collect(Collectors.toList());
    }

    public RatingSummary getWorkerRatingSummary(Long workerId) {
        Double avgRating = workerRatingRepository.getAverageRatingByWorkerId(workerId);
        Long totalRatings = workerRatingRepository.getRatingCountByWorkerId(workerId);

        return new RatingSummary(
                avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0,
                totalRatings != null ? totalRatings : 0L
        );
    }

    // ==================== HELPER METHODS ====================

    public List<OrderForRatingResponse> getCustomerOrdersForRating(Long customerId) {
        // Get all customer orders
        List<Order> orders = orderRepository.findByCustomer_Id(customerId);

        return orders.stream()
                .map(order -> {
                    OrderForRatingResponse response = new OrderForRatingResponse();
                    response.setOrderId(order.getOrderId());
                    response.setShopName(order.getShop().getShopName());
                    response.setDeadline(order.getDeadline());
                    response.setStatus(order.getStatus().toString());
                    response.setCreatedAt(order.getCreatedAt());

                    // Get tasks for this order
                    List<Task> tasks = taskRepository.findByOrder_OrderId(order.getOrderId());

                    // Map tasks to worker info
                    List<OrderForRatingResponse.WorkerTaskInfo> workerInfos = tasks.stream()
                            .map(task -> {
                                OrderForRatingResponse.WorkerTaskInfo workerInfo = 
                                    new OrderForRatingResponse.WorkerTaskInfo();
                                workerInfo.setWorkerId(task.getWorker().getId());
                                workerInfo.setWorkerName(task.getWorker().getUser().getName());
                                workerInfo.setTaskType(task.getTaskType().toString());
                                workerInfo.setTaskStatus(task.getStatus().toString());

                                // Check if already rated
                                boolean alreadyRated = workerRatingRepository
                                    .existsByWorker_IdAndOrder_OrderId(
                                        task.getWorker().getId(), 
                                        order.getOrderId()
                                    );
                                workerInfo.setAlreadyRated(alreadyRated);

                                return workerInfo;
                            })
                            .collect(Collectors.toList());

                    response.setWorkers(workerInfos);
                    return response;
                })
                .collect(Collectors.toList());
    }

    // ==================== HELPER METHODS ====================

    private RatingResponse mapToRatingResponse(ShopRating rating) {
        RatingResponse response = new RatingResponse();
        response.setRatingId(rating.getId());
        response.setOrderId(rating.getOrder().getOrderId());
        response.setCustomerName(rating.getCustomer().getUser().getName());
        response.setRating(rating.getRating());
        response.setReview(rating.getReview());
        response.setCreatedAt(rating.getCreatedAt());
        return response;
    }

    private RatingResponse mapToRatingResponse(WorkerRating rating) {
        RatingResponse response = new RatingResponse();
        response.setRatingId(rating.getId());
        response.setOrderId(rating.getOrder().getOrderId());
        response.setCustomerName(rating.getCustomer().getUser().getName());
        response.setRating(rating.getRating());
        response.setReview(rating.getReview());
        response.setCreatedAt(rating.getCreatedAt());
        return response;
    }
}
