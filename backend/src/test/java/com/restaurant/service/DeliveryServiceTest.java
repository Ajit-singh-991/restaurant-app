package com.restaurant.service;

import com.restaurant.dto.DeliveryDto.*;
import com.restaurant.entity.DeliveryTracking;
import com.restaurant.entity.DeliveryTracking.DeliveryStatus;
import com.restaurant.entity.Order;
import com.restaurant.entity.User;
import com.restaurant.repository.DeliveryTrackingRepository;
import com.restaurant.repository.OrderRepository;
import com.restaurant.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    @Mock
    private DeliveryTrackingRepository deliveryRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private DeliveryService deliveryService;

    private User testCustomer;
    private User testDriver;
    private Order testOrder;
    private DeliveryTracking testDelivery;

    @BeforeEach
    void setUp() {
        testCustomer = User.builder()
                .id(1L)
                .username("customer1")
                .fullName("Test Customer")
                .role(User.Role.CUSTOMER)
                .build();

        testDriver = User.builder()
                .id(50L)
                .username("driver1")
                .fullName("Test Driver")
                .role(User.Role.WAITER)
                .build();

        testOrder = Order.builder()
                .id(10L)
                .orderNumber("ORD-20260213-0001")
                .status(Order.OrderStatus.CONFIRMED)
                .orderType(Order.OrderType.DELIVERY)
                .customer(testCustomer)
                .totalAmount(new BigDecimal("45.00"))
                .build();

        testDelivery = DeliveryTracking.builder()
                .id(100L)
                .order(testOrder)
                .status(DeliveryStatus.PENDING)
                .deliveryAddress("123 Main St")
                .city("Springfield")
                .postalCode("12345")
                .contactPhone("555-1234")
                .deliveryFee(new BigDecimal("5.00"))
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ---- createDelivery tests ----

    @Test
    void createDelivery_success_returnsDeliveryResponse() {
        CreateDeliveryRequest request = CreateDeliveryRequest.builder()
                .orderId(10L)
                .deliveryAddress("123 Main St")
                .city("Springfield")
                .postalCode("12345")
                .contactPhone("555-1234")
                .deliveryInstructions("Ring the bell")
                .build();

        when(orderRepository.findById(10L)).thenReturn(Optional.of(testOrder));
        when(deliveryRepository.findByOrderId(10L)).thenReturn(Optional.empty());
        when(deliveryRepository.save(any(DeliveryTracking.class))).thenReturn(testDelivery);

        DeliveryResponse response = deliveryService.createDelivery(request);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(10L, response.getOrderId());
        assertEquals("ORD-20260213-0001", response.getOrderNumber());
        assertEquals("PENDING", response.getStatus());
        assertEquals("123 Main St", response.getDeliveryAddress());
        assertEquals("Springfield", response.getCity());
        assertEquals("12345", response.getPostalCode());
        assertEquals(new BigDecimal("5.00"), response.getDeliveryFee());
        assertNull(response.getDriverId());
        assertNull(response.getDriverName());
        verify(deliveryRepository).save(any(DeliveryTracking.class));
    }

    @Test
    void createDelivery_orderNotFound_throwsException() {
        CreateDeliveryRequest request = CreateDeliveryRequest.builder()
                .orderId(999L)
                .deliveryAddress("123 Main St")
                .build();

        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> deliveryService.createDelivery(request));

        assertEquals("Order not found", exception.getMessage());
        verify(deliveryRepository, never()).save(any());
    }

    @Test
    void createDelivery_nonDeliveryOrder_throwsException() {
        testOrder.setOrderType(Order.OrderType.DINE_IN);

        CreateDeliveryRequest request = CreateDeliveryRequest.builder()
                .orderId(10L)
                .deliveryAddress("123 Main St")
                .build();

        when(orderRepository.findById(10L)).thenReturn(Optional.of(testOrder));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> deliveryService.createDelivery(request));

        assertEquals("Order is not a delivery order", exception.getMessage());
        verify(deliveryRepository, never()).save(any());
    }

    @Test
    void createDelivery_duplicateDelivery_throwsException() {
        CreateDeliveryRequest request = CreateDeliveryRequest.builder()
                .orderId(10L)
                .deliveryAddress("123 Main St")
                .build();

        when(orderRepository.findById(10L)).thenReturn(Optional.of(testOrder));
        when(deliveryRepository.findByOrderId(10L)).thenReturn(Optional.of(testDelivery));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> deliveryService.createDelivery(request));

        assertEquals("Delivery tracking already exists for this order", exception.getMessage());
        verify(deliveryRepository, never()).save(any());
    }

    // ---- assignDriver tests ----

    @Test
    void assignDriver_success_returnsUpdatedDelivery() {
        when(deliveryRepository.findById(100L)).thenReturn(Optional.of(testDelivery));
        when(userRepository.findById(50L)).thenReturn(Optional.of(testDriver));
        when(deliveryRepository.save(any(DeliveryTracking.class))).thenAnswer(inv -> inv.getArgument(0));

        DeliveryResponse response = deliveryService.assignDriver(100L, 50L, 30);

        assertNotNull(response);
        assertEquals(50L, response.getDriverId());
        assertEquals("Test Driver", response.getDriverName());
        assertEquals("ASSIGNED", response.getStatus());
        assertEquals(30, response.getEstimatedMinutes());
        assertNotNull(response.getAssignedAt());
        verify(deliveryRepository).save(any(DeliveryTracking.class));
        verify(notificationService).notifyOrderStatusChanged(testOrder);
    }

    @Test
    void assignDriver_deliveryNotFound_throwsException() {
        when(deliveryRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> deliveryService.assignDriver(999L, 50L, 30));

        assertEquals("Delivery not found", exception.getMessage());
        verify(deliveryRepository, never()).save(any());
    }

    @Test
    void assignDriver_driverNotFound_throwsException() {
        when(deliveryRepository.findById(100L)).thenReturn(Optional.of(testDelivery));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> deliveryService.assignDriver(100L, 999L, 30));

        assertEquals("Driver not found", exception.getMessage());
        verify(deliveryRepository, never()).save(any());
    }

    // ---- updateStatus tests ----

    @Test
    void updateStatus_pickedUp_setsPickedUpAt() {
        testDelivery.setStatus(DeliveryStatus.ASSIGNED);
        testDelivery.setDriver(testDriver);
        testDelivery.setAssignedAt(LocalDateTime.now().minusMinutes(10));

        when(deliveryRepository.findById(100L)).thenReturn(Optional.of(testDelivery));
        when(deliveryRepository.save(any(DeliveryTracking.class))).thenAnswer(inv -> inv.getArgument(0));

        DeliveryResponse response = deliveryService.updateStatus(100L, DeliveryStatus.PICKED_UP);

        assertEquals("PICKED_UP", response.getStatus());
        assertNotNull(response.getPickedUpAt());
        verify(orderRepository, never()).save(any());
        verify(notificationService).notifyOrderStatusChanged(testOrder);
    }

    @Test
    void updateStatus_delivered_completesOrder() {
        testDelivery.setStatus(DeliveryStatus.IN_TRANSIT);
        testDelivery.setDriver(testDriver);
        testDelivery.setAssignedAt(LocalDateTime.now().minusMinutes(30));
        testDelivery.setPickedUpAt(LocalDateTime.now().minusMinutes(20));

        when(deliveryRepository.findById(100L)).thenReturn(Optional.of(testDelivery));
        when(deliveryRepository.save(any(DeliveryTracking.class))).thenAnswer(inv -> inv.getArgument(0));

        DeliveryResponse response = deliveryService.updateStatus(100L, DeliveryStatus.DELIVERED);

        assertEquals("DELIVERED", response.getStatus());
        assertNotNull(response.getDeliveredAt());
        assertEquals(Order.OrderStatus.COMPLETED, testOrder.getStatus());
        assertNotNull(testOrder.getCompletedAt());
        verify(orderRepository).save(testOrder);
        verify(notificationService).notifyOrderStatusChanged(testOrder);
    }

    @Test
    void updateStatus_cancelled_cancelsOrder() {
        testDelivery.setStatus(DeliveryStatus.ASSIGNED);
        testDelivery.setDriver(testDriver);

        when(deliveryRepository.findById(100L)).thenReturn(Optional.of(testDelivery));
        when(deliveryRepository.save(any(DeliveryTracking.class))).thenAnswer(inv -> inv.getArgument(0));

        DeliveryResponse response = deliveryService.updateStatus(100L, DeliveryStatus.CANCELLED);

        assertEquals("CANCELLED", response.getStatus());
        assertEquals(Order.OrderStatus.CANCELLED, testOrder.getStatus());
        verify(orderRepository).save(testOrder);
        verify(notificationService).notifyOrderStatusChanged(testOrder);
    }

    @Test
    void updateStatus_deliveryNotFound_throwsException() {
        when(deliveryRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> deliveryService.updateStatus(999L, DeliveryStatus.PICKED_UP));

        assertEquals("Delivery not found", exception.getMessage());
        verify(deliveryRepository, never()).save(any());
    }

    // ---- getByOrderId tests ----

    @Test
    void getByOrderId_found_returnsDeliveryResponse() {
        when(deliveryRepository.findByOrderId(10L)).thenReturn(Optional.of(testDelivery));

        DeliveryResponse response = deliveryService.getByOrderId(10L);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(10L, response.getOrderId());
        assertEquals("ORD-20260213-0001", response.getOrderNumber());
    }

    @Test
    void getByOrderId_notFound_throwsException() {
        when(deliveryRepository.findByOrderId(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> deliveryService.getByOrderId(999L));

        assertEquals("Delivery not found for this order", exception.getMessage());
    }

    // ---- getActiveDeliveries tests ----

    @Test
    void getActiveDeliveries_returnsListOfDeliveries() {
        DeliveryTracking delivery2 = DeliveryTracking.builder()
                .id(101L)
                .order(Order.builder().id(11L).orderNumber("ORD-20260213-0002")
                        .orderType(Order.OrderType.DELIVERY).build())
                .status(DeliveryStatus.ASSIGNED)
                .deliveryAddress("456 Oak Ave")
                .driver(testDriver)
                .deliveryFee(new BigDecimal("5.00"))
                .createdAt(LocalDateTime.now())
                .build();

        when(deliveryRepository.findActiveDeliveries())
                .thenReturn(List.of(testDelivery, delivery2));

        List<DeliveryResponse> result = deliveryService.getActiveDeliveries();

        assertEquals(2, result.size());
        assertEquals(100L, result.get(0).getId());
        assertEquals(101L, result.get(1).getId());
    }

    @Test
    void getActiveDeliveries_noActive_returnsEmptyList() {
        when(deliveryRepository.findActiveDeliveries()).thenReturn(List.of());

        List<DeliveryResponse> result = deliveryService.getActiveDeliveries();

        assertTrue(result.isEmpty());
    }

    // ---- getDeliveryStats tests ----

    @Test
    void getDeliveryStats_withDeliveries_returnsCorrectStats() {
        LocalDateTime now = LocalDateTime.now();

        DeliveryTracking completedDelivery = DeliveryTracking.builder()
                .id(102L)
                .order(Order.builder().id(12L).orderNumber("ORD-20260213-0003")
                        .orderType(Order.OrderType.DELIVERY).build())
                .status(DeliveryStatus.DELIVERED)
                .driver(testDriver)
                .deliveryAddress("789 Elm St")
                .deliveryFee(new BigDecimal("5.00"))
                .assignedAt(now.minusMinutes(45))
                .deliveredAt(now.minusMinutes(5))
                .createdAt(now.minusHours(1))
                .build();

        when(deliveryRepository.findAll()).thenReturn(List.of(testDelivery, completedDelivery));
        when(deliveryRepository.findActiveDeliveries()).thenReturn(List.of(testDelivery));

        DeliveryStats stats = deliveryService.getDeliveryStats();

        assertEquals(2, stats.getTotalDeliveries());
        assertEquals(1, stats.getActiveDeliveries());
        assertEquals(1, stats.getCompletedToday());
        assertTrue(stats.getAvgDeliveryTimeMinutes() > 0);
        assertNotNull(stats.getDriverPerformance());
        assertEquals(1, stats.getDriverPerformance().size());
        assertEquals("Test Driver", stats.getDriverPerformance().get(0).getDriverName());
    }

    @Test
    void getDeliveryStats_noDeliveries_returnsZeroStats() {
        when(deliveryRepository.findAll()).thenReturn(List.of());
        when(deliveryRepository.findActiveDeliveries()).thenReturn(List.of());

        DeliveryStats stats = deliveryService.getDeliveryStats();

        assertEquals(0, stats.getTotalDeliveries());
        assertEquals(0, stats.getActiveDeliveries());
        assertEquals(0, stats.getCompletedToday());
        assertEquals(0.0, stats.getAvgDeliveryTimeMinutes());
        assertTrue(stats.getDriverPerformance().isEmpty());
    }
}
