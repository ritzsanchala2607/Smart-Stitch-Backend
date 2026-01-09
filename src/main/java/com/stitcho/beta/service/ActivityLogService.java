package com.stitcho.beta.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stitcho.beta.Repository.OrderActivityRepository;
import com.stitcho.beta.entity.ActivityType;
import com.stitcho.beta.entity.Order;
import com.stitcho.beta.entity.OrderActivity;
import com.stitcho.beta.entity.OrderStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ActivityLogService {
    
    private final OrderActivityRepository activityRepository;

    /**
     * Log order creation activity
     */
    @Transactional
    public void logOrderCreated(Order order) {
        OrderActivity activity = new OrderActivity();
        activity.setOrder(order);
        activity.setActivityType(ActivityType.ORDER_CREATED);
        activity.setDescription("New order created");
        activity.setNewStatus(order.getStatus().name());
        activityRepository.save(activity);
    }

    /**
     * Log order status change activity
     */
    @Transactional
    public void logStatusChange(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        OrderActivity activity = new OrderActivity();
        activity.setOrder(order);
        activity.setActivityType(ActivityType.STATUS_CHANGED);
        activity.setOldStatus(oldStatus != null ? oldStatus.name() : null);
        activity.setNewStatus(newStatus.name());
        
        // Generate user-friendly description
        String description = generateStatusChangeDescription(newStatus, order.getDeadline());
        activity.setDescription(description);
        
        activityRepository.save(activity);
    }

    /**
     * Log payment received activity
     */
    @Transactional
    public void logPaymentReceived(Order order, Double amount) {
        OrderActivity activity = new OrderActivity();
        activity.setOrder(order);
        activity.setActivityType(ActivityType.PAYMENT_RECEIVED);
        activity.setDescription("Payment of ₹" + amount + " received");
        activityRepository.save(activity);
    }

    /**
     * Log order delivered activity
     */
    @Transactional
    public void logOrderDelivered(Order order) {
        OrderActivity activity = new OrderActivity();
        activity.setOrder(order);
        activity.setActivityType(ActivityType.ORDER_DELIVERED);
        activity.setDescription("Order delivered successfully");
        activity.setNewStatus(OrderStatus.DELIVERED.name());
        activityRepository.save(activity);
    }

    /**
     * Log order cancelled activity
     */
    @Transactional
    public void logOrderCancelled(Order order, String reason) {
        OrderActivity activity = new OrderActivity();
        activity.setOrder(order);
        activity.setActivityType(ActivityType.ORDER_CANCELLED);
        activity.setDescription("Order cancelled" + (reason != null ? ": " + reason : ""));
        activity.setNewStatus(OrderStatus.CANCELLED.name());
        activityRepository.save(activity);
    }

    /**
     * Generate user-friendly status change description
     */
    private String generateStatusChangeDescription(OrderStatus newStatus, LocalDate deadline) {
        String baseDescription;
        
        switch (newStatus) {
            case CUTTING:
                baseDescription = "Your order progressed to Cutting stage";
                break;
            case STITCHING:
                baseDescription = "Your order progressed to Stitching stage";
                break;
            case IRONING:
                baseDescription = "Your order progressed to Ironing stage";
                break;
            case COMPLETED:
                baseDescription = "Your order is completed and ready for pickup";
                break;
            case DELIVERED:
                baseDescription = "Your order has been delivered";
                break;
            case CANCELLED:
                baseDescription = "Your order has been cancelled";
                break;
            default:
                baseDescription = "Order status updated to " + newStatus.name();
        }

        // Add deadline information if applicable
        if (deadline != null && (newStatus == OrderStatus.COMPLETED || newStatus == OrderStatus.STITCHING)) {
            long daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), deadline);
            if (daysUntil >= 0 && daysUntil <= 3) {
                baseDescription += " - Delivery expected in " + daysUntil + " day" + (daysUntil != 1 ? "s" : "");
            }
        }

        return baseDescription;
    }
}
