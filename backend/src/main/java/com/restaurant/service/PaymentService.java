package com.restaurant.service;

import com.restaurant.dto.PaymentDto;
import com.restaurant.entity.Invoice;
import com.restaurant.entity.Order;
import com.restaurant.entity.Payment;
import com.restaurant.repository.OrderRepository;
import com.restaurant.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;
    private final InvoiceService invoiceService;
    private final ReceiptEmailService receiptEmailService;

    @Transactional
    public Payment processPayment(PaymentDto.PaymentRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        paymentRepository.findByOrderId(order.getId()).ifPresent(existing -> {
            if (existing.getStatus() == Payment.PaymentStatus.SUCCESS) {
                throw new IllegalArgumentException("Order already paid");
            }
        });

        Payment payment = Payment.builder()
                .order(order)
                .amount(order.getTotalAmount())
                .paymentMethod(Payment.PaymentMethod.valueOf(request.getPaymentMethod()))
                .transactionId(request.getTransactionId())
                .status(Payment.PaymentStatus.SUCCESS)
                .build();

        Payment saved = paymentRepository.save(payment);

        order.setStatus(Order.OrderStatus.COMPLETED);
        order.setCompletedAt(LocalDateTime.now());
        orderRepository.save(order);

        try {
            invoiceService.generateInvoice(order.getId(), null);
        } catch (Exception e) {
            // Log but do not fail payment if invoice generation fails
        }

        String recipientEmail = request.getRecipientEmail();
        if (recipientEmail == null && order.getCustomer() != null) {
            recipientEmail = order.getCustomer().getEmail();
        }
        receiptEmailService.sendReceiptIfEnabled(order, saved, recipientEmail);

        notificationService.notifyPaymentProcessed(order, saved);

        return saved;
    }

    @Transactional(readOnly = true)
    public Payment getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found for order"));
    }

    @Transactional(readOnly = true)
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @Transactional
    public Payment refundPayment(Long paymentId, String reason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        if (payment.getStatus() != Payment.PaymentStatus.SUCCESS) {
            throw new IllegalArgumentException("Can only refund successful payments");
        }

        payment.setStatus(Payment.PaymentStatus.REFUNDED);
        payment.setRefundedAt(LocalDateTime.now());
        payment.setRefundReason(reason);

        return paymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public PaymentDto.PaymentStats getPaymentStats() {
        LocalDate today = LocalDate.now();
        LocalDateTime dayStart = today.atStartOfDay();
        LocalDateTime dayEnd = today.atTime(LocalTime.MAX);
        LocalDateTime weekStart = today.minusDays(7).atStartOfDay();

        PaymentDto.PaymentStats stats = new PaymentDto.PaymentStats();
        stats.setDailySales(paymentRepository.sumSuccessfulPaymentsBetween(dayStart, dayEnd));
        stats.setDailyTransactions(paymentRepository.countSuccessfulPaymentsBetween(dayStart, dayEnd));
        stats.setWeeklySales(paymentRepository.sumSuccessfulPaymentsBetween(weekStart, dayEnd));
        stats.setWeeklyTransactions(paymentRepository.countSuccessfulPaymentsBetween(weekStart, dayEnd));

        if (stats.getDailyTransactions() > 0) {
            stats.setAverageOrderValue(stats.getDailySales()
                    .divide(BigDecimal.valueOf(stats.getDailyTransactions()), 2, RoundingMode.HALF_UP));
        } else {
            stats.setAverageOrderValue(BigDecimal.ZERO);
        }

        return stats;
    }

    @Transactional(readOnly = true)
    public BigDecimal getDailySales() {
        LocalDate today = LocalDate.now();
        LocalDateTime dayStart = today.atStartOfDay();
        LocalDateTime dayEnd = today.atTime(LocalTime.MAX);
        BigDecimal sum = paymentRepository.sumSuccessfulPaymentsBetween(dayStart, dayEnd);
        return sum != null ? sum : BigDecimal.ZERO;
    }
}
