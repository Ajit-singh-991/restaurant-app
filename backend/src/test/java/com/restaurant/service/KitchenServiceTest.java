package com.restaurant.service;

import com.restaurant.dto.KitchenDto;
import com.restaurant.entity.Order;
import com.restaurant.entity.OrderItem;
import com.restaurant.entity.MenuItem;
import com.restaurant.entity.Category;
import com.restaurant.repository.OrderItemRepository;
import com.restaurant.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KitchenServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private KitchenService kitchenService;

    private Order testOrder;
    private OrderItem item1;
    private OrderItem item2;

    @BeforeEach
    void setUp() {
        Category category = Category.builder().id(1L).name("Main Course").build();
        MenuItem menuItem = MenuItem.builder()
                .id(1L).name("Butter Chicken").price(new BigDecimal("350.00"))
                .category(category).available(true).build();

        item1 = OrderItem.builder()
                .id(1L).menuItem(menuItem).quantity(2)
                .unitPrice(new BigDecimal("350.00"))
                .status(OrderItem.ItemStatus.PREPARING)
                .build();

        item2 = OrderItem.builder()
                .id(2L).menuItem(menuItem).quantity(1)
                .unitPrice(new BigDecimal("350.00"))
                .status(OrderItem.ItemStatus.PREPARING)
                .build();

        List<OrderItem> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);

        testOrder = Order.builder()
                .id(1L)
                .orderNumber("ORD-20260212-0001")
                .status(Order.OrderStatus.PREPARING)
                .items(items)
                .createdAt(LocalDateTime.now().minusMinutes(10))
                .build();

        item1.setOrder(testOrder);
        item2.setOrder(testOrder);
    }

    @Test
    void markItemComplete_singleItem_marksItemReady() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderItem result = kitchenService.markItemComplete(1L, 1L);

        assertEquals(OrderItem.ItemStatus.READY, result.getStatus());
        // Order should still be PREPARING since item2 is not ready
        assertEquals(Order.OrderStatus.PREPARING, testOrder.getStatus());
        verify(notificationService).notifyOrderStatusChanged(testOrder);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void markItemComplete_lastItem_autoMarksOrderReady() {
        // item1 is already READY
        item1.setStatus(OrderItem.ItemStatus.READY);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderItem result = kitchenService.markItemComplete(1L, 2L);

        assertEquals(OrderItem.ItemStatus.READY, result.getStatus());
        assertEquals(Order.OrderStatus.READY, testOrder.getStatus());
        verify(orderRepository).save(testOrder);
        verify(notificationService).notifyOrderStatusChanged(testOrder);
    }

    @Test
    void markItemComplete_withCancelledItem_ignoresCancelledForAutoReady() {
        item2.setStatus(OrderItem.ItemStatus.CANCELLED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderItem result = kitchenService.markItemComplete(1L, 1L);

        assertEquals(OrderItem.ItemStatus.READY, result.getStatus());
        // Only non-cancelled item is now READY, so order auto-completes
        assertEquals(Order.OrderStatus.READY, testOrder.getStatus());
        verify(orderRepository).save(testOrder);
    }

    @Test
    void markItemComplete_invalidOrderId_throwsException() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> kitchenService.markItemComplete(999L, 1L));
    }

    @Test
    void markItemComplete_invalidItemId_throwsException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        assertThrows(IllegalArgumentException.class,
                () -> kitchenService.markItemComplete(1L, 999L));
    }

    @Test
    void startPreparing_marksOrderAndItemsPreparing() {
        testOrder.setStatus(Order.OrderStatus.PENDING);
        item1.setStatus(OrderItem.ItemStatus.PENDING);
        item2.setStatus(OrderItem.ItemStatus.PENDING);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = kitchenService.startPreparing(1L);

        assertEquals(Order.OrderStatus.PREPARING, result.getStatus());
        assertEquals(OrderItem.ItemStatus.PREPARING, item1.getStatus());
        assertEquals(OrderItem.ItemStatus.PREPARING, item2.getStatus());
        verify(notificationService).notifyOrderStatusChanged(result);
    }

    @Test
    void markReady_marksOrderAndAllItemsReady() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = kitchenService.markReady(1L);

        assertEquals(Order.OrderStatus.READY, result.getStatus());
        assertEquals(OrderItem.ItemStatus.READY, item1.getStatus());
        assertEquals(OrderItem.ItemStatus.READY, item2.getStatus());
        verify(notificationService).notifyOrderStatusChanged(result);
    }

    @Test
    void getKitchenStats_returnsCorrectCounts() {
        Order preparingOrder = Order.builder().id(1L).status(Order.OrderStatus.PREPARING)
                .createdAt(LocalDateTime.now()).build();
        Order readyOrder = Order.builder().id(2L).status(Order.OrderStatus.READY)
                .createdAt(LocalDateTime.now()).build();
        Order pendingOrder = Order.builder().id(3L).status(Order.OrderStatus.PENDING)
                .createdAt(LocalDateTime.now()).build();

        when(orderRepository.findActiveOrders(any())).thenReturn(
                List.of(preparingOrder, readyOrder, pendingOrder));
        when(orderRepository.findByCreatedAtBetween(any(), any())).thenReturn(List.of());

        KitchenDto.KitchenStats stats = kitchenService.getKitchenStats();

        assertEquals(3, stats.getActiveOrders());
        assertEquals(1, stats.getPreparingOrders());
        assertEquals(1, stats.getReadyOrders());
        assertEquals(0.0, stats.getAvgPrepTimeMinutes());
    }

    @Test
    void getKitchenStats_calculatesAvgPrepTime() {
        when(orderRepository.findActiveOrders(any())).thenReturn(List.of());

        LocalDateTime now = LocalDateTime.now();
        Order completed1 = Order.builder().id(1L).status(Order.OrderStatus.COMPLETED)
                .createdAt(now.minusMinutes(20)).completedAt(now.minusMinutes(10)).build();
        Order completed2 = Order.builder().id(2L).status(Order.OrderStatus.COMPLETED)
                .createdAt(now.minusMinutes(30)).completedAt(now.minusMinutes(10)).build();

        when(orderRepository.findByCreatedAtBetween(any(), any()))
                .thenReturn(List.of(completed1, completed2));

        KitchenDto.KitchenStats stats = kitchenService.getKitchenStats();

        // Order 1: 10 min, Order 2: 20 min → avg = 15
        assertEquals(15.0, stats.getAvgPrepTimeMinutes());
    }
}
