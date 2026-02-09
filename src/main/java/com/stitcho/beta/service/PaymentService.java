package com.stitcho.beta.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stitcho.beta.Repository.OrderItemRepository;
import com.stitcho.beta.Repository.OrderRepository;
import com.stitcho.beta.Repository.OwnerRepository;
import com.stitcho.beta.Repository.PaymentRepository;
import com.stitcho.beta.Repository.UserRepository;
import com.stitcho.beta.dto.BillResponse;
import com.stitcho.beta.dto.OrderPaymentResponse;
import com.stitcho.beta.dto.PaymentResponse;
import com.stitcho.beta.dto.UpdatePaymentRequest;
import com.stitcho.beta.entity.Order;
import com.stitcho.beta.entity.OrderItem;
import com.stitcho.beta.entity.Owner;
import com.stitcho.beta.entity.Payment;
import com.stitcho.beta.entity.PaymentMethod;
import com.stitcho.beta.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OwnerRepository ownerRepository;
    private final UserRepository userRepository;

    @Transactional
    public OrderPaymentResponse updatePayment(Long userId, Long orderId, UpdatePaymentRequest request) {
        // Verify owner owns this order
        Owner owner = ownerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        if (!order.getShop().getShopId().equals(owner.getShop().getShopId())) {
            throw new RuntimeException("Access denied: Order does not belong to your shop");
        }

        // Validate payment amount
        double currentPaid = order.getPaidAmount() != null ? order.getPaidAmount() : 0.0;
        double totalPrice = order.getTotalPrice();
        double balance = totalPrice - currentPaid;

        if (request.getAdditionalPayment() > balance + 0.01) { // Allow small floating point difference
            throw new IllegalArgumentException("Payment amount exceeds remaining balance");
        }

        // Create payment record
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(request.getAdditionalPayment());
        payment.setPaymentMethod(PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase()));
        payment.setPaymentDate(request.getPaymentDate() != null ? request.getPaymentDate() : LocalDateTime.now());
        payment.setNote(request.getPaymentNote());
        payment.setRecordedBy(user);
        paymentRepository.save(payment);

        // Update order payment info
        double newPaidAmount = currentPaid + request.getAdditionalPayment();
        order.setPaidAmount(newPaidAmount);
        
        // Update payment status
        if (newPaidAmount >= totalPrice - 0.01) { // Allow small floating point difference
            order.setPaymentStatus("PAID");
        } else if (newPaidAmount > 0) {
            order.setPaymentStatus("PARTIAL");
        } else {
            order.setPaymentStatus("PENDING");
        }
        
        orderRepository.save(order);

        // Return updated payment info
        return getOrderPaymentInfo(orderId);
    }

    public OrderPaymentResponse getOrderPaymentInfo(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        List<Payment> payments = paymentRepository.findByOrder_OrderIdOrderByPaymentDateDesc(orderId);
        
        double paidAmount = order.getPaidAmount() != null ? order.getPaidAmount() : 0.0;
        double balanceAmount = order.getTotalPrice() - paidAmount;

        List<PaymentResponse> paymentResponses = payments.stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());

        return new OrderPaymentResponse(
                order.getOrderId(),
                order.getTotalPrice(),
                paidAmount,
                balanceAmount,
                order.getPaymentStatus(),
                paymentResponses
        );
    }

    public List<PaymentResponse> getPaymentHistory(Long userId, Long orderId) {
        // Verify owner owns this order
        Owner owner = ownerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        if (!order.getShop().getShopId().equals(owner.getShop().getShopId())) {
            throw new RuntimeException("Access denied: Order does not belong to your shop");
        }

        List<Payment> payments = paymentRepository.findByOrder_OrderIdOrderByPaymentDateDesc(orderId);
        
        return payments.stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());
    }

    public BillResponse generateBill(Long userId, Long orderId) {
        // Verify owner owns this order
        Owner owner = ownerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        if (!order.getShop().getShopId().equals(owner.getShop().getShopId())) {
            throw new RuntimeException("Access denied: Order does not belong to your shop");
        }

        BillResponse bill = new BillResponse();
        
        // Bill metadata
        bill.setBillNumber("BILL-" + order.getCreatedAt().getYear() + "-" + 
                String.format("%05d", order.getOrderId()));
        bill.setBillDate(LocalDateTime.now());

        // Shop info
        BillResponse.ShopInfo shopInfo = new BillResponse.ShopInfo();
        shopInfo.setShopId(order.getShop().getShopId());
        shopInfo.setName(order.getShop().getShopName());
        shopInfo.setAddress(order.getShop().getShopAddress());
        shopInfo.setContactNumber(order.getShop().getShopMobileNo());
        shopInfo.setEmail(order.getShop().getShopEmail());
        shopInfo.setGstNumber(order.getShop().getGstNumber());
        bill.setShop(shopInfo);

        // Customer info
        BillResponse.CustomerInfo customerInfo = new BillResponse.CustomerInfo();
        customerInfo.setCustomerId(order.getCustomer().getId());
        customerInfo.setName(order.getCustomer().getUser().getName());
        customerInfo.setContactNumber(order.getCustomer().getUser().getContactNumber());
        customerInfo.setEmail(order.getCustomer().getUser().getEmail());
        bill.setCustomer(customerInfo);

        // Order info
        BillResponse.OrderInfo orderInfo = new BillResponse.OrderInfo();
        orderInfo.setOrderId(order.getOrderId());
        orderInfo.setOrderNumber("ORD" + String.format("%03d", order.getOrderId()));
        orderInfo.setOrderDate(order.getCreatedAt());
        orderInfo.setDeadline(order.getDeadline());
        orderInfo.setStatus(order.getStatus() != null ? order.getStatus().name() : "NEW");
        bill.setOrder(orderInfo);

        // Items
        List<OrderItem> items = orderItemRepository.findByOrder_OrderId(orderId);
        List<BillResponse.ItemInfo> itemInfos = items.stream()
                .map(item -> new BillResponse.ItemInfo(
                        item.getItemId(),
                        item.getItemName(),
                        item.getQuantity(),
                        item.getPrice(),
                        item.getPrice() * item.getQuantity(),
                        item.getFabricType(),
                        item.getItemName() + " - " + item.getFabricType()
                ))
                .collect(Collectors.toList());
        bill.setItems(itemInfos);

        // Pricing
        double subtotal = order.getTotalPrice();
        BillResponse.PricingInfo pricing = new BillResponse.PricingInfo();
        pricing.setSubtotal(subtotal);
        pricing.setDiscount(0.0);
        pricing.setTaxRate(0.0);
        pricing.setTaxAmount(0.0);
        pricing.setTotalAmount(subtotal);
        bill.setPricing(pricing);

        // Payment info
        List<Payment> payments = paymentRepository.findByOrder_OrderIdOrderByPaymentDateDesc(orderId);
        List<BillResponse.PaymentDetail> paymentDetails = payments.stream()
                .map(p -> new BillResponse.PaymentDetail(
                        p.getAmount(),
                        p.getPaymentMethod().name(),
                        p.getPaymentDate(),
                        p.getNote()
                ))
                .collect(Collectors.toList());

        double paidAmount = order.getPaidAmount() != null ? order.getPaidAmount() : 0.0;
        BillResponse.PaymentInfo paymentInfo = new BillResponse.PaymentInfo();
        paymentInfo.setTotalPrice(order.getTotalPrice());
        paymentInfo.setPaidAmount(paidAmount);
        paymentInfo.setBalanceAmount(order.getTotalPrice() - paidAmount);
        paymentInfo.setPaymentStatus(order.getPaymentStatus());
        paymentInfo.setPayments(paymentDetails);
        bill.setPayment(paymentInfo);

        // Notes and terms
        bill.setNotes(order.getNotes());
        bill.setTerms("All sales are final. No refunds or exchanges.");

        return bill;
    }

    private PaymentResponse mapToPaymentResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setPaymentId(payment.getPaymentId());
        response.setAmount(payment.getAmount());
        response.setPaymentMethod(payment.getPaymentMethod().name());
        response.setPaymentDate(payment.getPaymentDate());
        response.setNote(payment.getNote());
        response.setRecordedBy(payment.getRecordedBy() != null ? 
                payment.getRecordedBy().getName() : "System");
        response.setCreatedAt(payment.getCreatedAt());
        return response;
    }
}
