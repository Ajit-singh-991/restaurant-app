package com.restaurant.service;

import com.restaurant.dto.PaymentDto;
import com.restaurant.entity.Order;
import com.restaurant.entity.Payment;
import com.restaurant.repository.OrderRepository;
import com.restaurant.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private InvoiceService invoiceService;

    @Mock
    private ReceiptEmailService receiptEmailService;

    @InjectMocks
    private PaymentService paymentService;

    private Order testOrder;
    private Payment testPayment;

    @BeforeEach
    void setUp() {
        testOrder = Order.builder()
                .id(1L)
                .orderNumber("ORD-20260211-0001")
                .status(Order.OrderStatus.SERVED)
                .totalAmount(new BigDecimal("770.00"))
                .build();

        testPayment = Payment.builder()
                .id(1L)
                .order(testOrder)
                .amount(new BigDecimal("770.00"))
                .paymentMethod(Payment.PaymentMethod.CASH)
                .status(Payment.PaymentStatus.SUCCESS)
                .build();
    }

    @Test
    void processPayment_withValidRequest_createsPayment() {
        PaymentDto.PaymentRequest request = new PaymentDto.PaymentRequest();
        request.setOrderId(1L);
        request.setPaymentMethod("CASH");

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Payment result = paymentService.processPayment(request);

        assertNotNull(result);
        assertEquals(new BigDecimal("770.00"), result.getAmount());
        assertEquals(Payment.PaymentMethod.CASH, result.getPaymentMethod());
        assertEquals(Payment.PaymentStatus.SUCCESS, result.getStatus());
        assertEquals(Order.OrderStatus.COMPLETED, testOrder.getStatus());
        assertNotNull(testOrder.getCompletedAt());
        verify(notificationService).notifyPaymentProcessed(eq(testOrder), any(Payment.class));
    }

    @Test
    void processPayment_withUpiAndTransactionId_setsTransactionId() {
        PaymentDto.PaymentRequest request = new PaymentDto.PaymentRequest();
        request.setOrderId(1L);
        request.setPaymentMethod("UPI");
        request.setTransactionId("TXN-12345");

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Payment result = paymentService.processPayment(request);

        assertEquals(Payment.PaymentMethod.UPI, result.getPaymentMethod());
        assertEquals("TXN-12345", result.getTransactionId());
    }

    @Test
    void processPayment_withAlreadyPaidOrder_throwsException() {
        PaymentDto.PaymentRequest request = new PaymentDto.PaymentRequest();
        request.setOrderId(1L);
        request.setPaymentMethod("CASH");

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.of(testPayment));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> paymentService.processPayment(request));

        assertEquals("Order already paid", ex.getMessage());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void processPayment_withInvalidOrderId_throwsException() {
        PaymentDto.PaymentRequest request = new PaymentDto.PaymentRequest();
        request.setOrderId(999L);
        request.setPaymentMethod("CASH");

        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> paymentService.processPayment(request));
    }

    @Test
    void getPaymentByOrderId_withValidOrder_returnsPayment() {
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.of(testPayment));

        Payment result = paymentService.getPaymentByOrderId(1L);

        assertEquals(testPayment, result);
        assertEquals(new BigDecimal("770.00"), result.getAmount());
    }

    @Test
    void getPaymentByOrderId_withNoPayment_throwsException() {
        when(paymentRepository.findByOrderId(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> paymentService.getPaymentByOrderId(999L));
    }

    @Test
    void getAllPayments_returnsAllPayments() {
        when(paymentRepository.findAll()).thenReturn(List.of(testPayment));

        List<Payment> result = paymentService.getAllPayments();

        assertEquals(1, result.size());
    }

    @Test
    void refundPayment_withSuccessfulPayment_refundsSuccessfully() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        Payment result = paymentService.refundPayment(1L, "Customer requested refund");

        assertEquals(Payment.PaymentStatus.REFUNDED, result.getStatus());
        assertNotNull(result.getRefundedAt());
        assertEquals("Customer requested refund", result.getRefundReason());
    }

    @Test
    void refundPayment_withNonSuccessfulPayment_throwsException() {
        testPayment.setStatus(Payment.PaymentStatus.PENDING);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> paymentService.refundPayment(1L, "Reason"));

        assertEquals("Can only refund successful payments", ex.getMessage());
    }

    @Test
    void refundPayment_withInvalidId_throwsException() {
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> paymentService.refundPayment(999L, "Reason"));
    }

    @Test
    void getPaymentStats_withTransactions_calculatesCorrectly() {
        when(paymentRepository.sumSuccessfulPaymentsBetween(any(), any()))
                .thenReturn(new BigDecimal("5000.00"))
                .thenReturn(new BigDecimal("25000.00"));
        when(paymentRepository.countSuccessfulPaymentsBetween(any(), any()))
                .thenReturn(10L)
                .thenReturn(50L);

        PaymentDto.PaymentStats stats = paymentService.getPaymentStats();

        assertEquals(new BigDecimal("5000.00"), stats.getDailySales());
        assertEquals(10L, stats.getDailyTransactions());
        assertEquals(new BigDecimal("25000.00"), stats.getWeeklySales());
        assertEquals(50L, stats.getWeeklyTransactions());
        assertEquals(new BigDecimal("500.00"), stats.getAverageOrderValue());
    }

    @Test
    void getPaymentStats_withNoTransactions_returnsZeroAverage() {
        when(paymentRepository.sumSuccessfulPaymentsBetween(any(), any()))
                .thenReturn(BigDecimal.ZERO)
                .thenReturn(BigDecimal.ZERO);
        when(paymentRepository.countSuccessfulPaymentsBetween(any(), any()))
                .thenReturn(0L)
                .thenReturn(0L);

        PaymentDto.PaymentStats stats = paymentService.getPaymentStats();

        assertEquals(BigDecimal.ZERO, stats.getDailySales());
        assertEquals(0L, stats.getDailyTransactions());
        assertEquals(BigDecimal.ZERO, stats.getAverageOrderValue());
    }
}
