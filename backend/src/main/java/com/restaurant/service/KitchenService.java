package com.restaurant.service;

import com.restaurant.dto.KitchenDto;
import com.restaurant.entity.KitchenStation;
import com.restaurant.entity.Order;
import com.restaurant.entity.OrderItem;
import com.restaurant.repository.KitchenStationRepository;
import com.restaurant.repository.OrderItemRepository;
import com.restaurant.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KitchenService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final KitchenStationRepository kitchenStationRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<Order> getActiveOrders() {
        return orderRepository.findActiveOrders(List.of(
                Order.OrderStatus.PENDING,
                Order.OrderStatus.CONFIRMED,
                Order.OrderStatus.PREPARING,
                Order.OrderStatus.READY
        ));
    }

    @Transactional(readOnly = true)
    public List<KitchenStation> getStations() {
        return kitchenStationRepository.findAllByOrderByDisplayOrderAsc();
    }

    @Transactional(readOnly = true)
    public List<Order> getActiveOrdersByStation(Long stationId) {
        return orderRepository.findActiveOrdersByStationId(stationId, List.of(
                Order.OrderStatus.PENDING,
                Order.OrderStatus.CONFIRMED,
                Order.OrderStatus.PREPARING,
                Order.OrderStatus.READY
        ));
    }

    @Transactional
    public Order startPreparing(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        order.setStatus(Order.OrderStatus.PREPARING);

        // Mark all items as PREPARING too
        for (OrderItem item : order.getItems()) {
            if (item.getStatus() == OrderItem.ItemStatus.PENDING) {
                item.setStatus(OrderItem.ItemStatus.PREPARING);
            }
        }

        Order updated = orderRepository.save(order);
        notificationService.notifyOrderStatusChanged(updated);
        return updated;
    }

    @Transactional
    public Order markReady(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        order.setStatus(Order.OrderStatus.READY);

        // Mark all items as READY too
        for (OrderItem item : order.getItems()) {
            if (item.getStatus() != OrderItem.ItemStatus.CANCELLED) {
                item.setStatus(OrderItem.ItemStatus.READY);
            }
        }

        Order updated = orderRepository.save(order);
        notificationService.notifyOrderStatusChanged(updated);
        return updated;
    }

    @Transactional
    public OrderItem markItemComplete(Long orderId, Long itemId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        OrderItem targetItem = order.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Order item not found"));

        targetItem.setStatus(OrderItem.ItemStatus.READY);
        orderItemRepository.save(targetItem);

        // Check if all non-cancelled items are READY — auto-mark order as READY
        boolean allReady = order.getItems().stream()
                .filter(item -> item.getStatus() != OrderItem.ItemStatus.CANCELLED)
                .allMatch(item -> item.getStatus() == OrderItem.ItemStatus.READY);

        if (allReady) {
            order.setStatus(Order.OrderStatus.READY);
            orderRepository.save(order);
            notificationService.notifyOrderStatusChanged(order);
        } else {
            notificationService.notifyOrderStatusChanged(order);
        }

        return targetItem;
    }

    @Transactional(readOnly = true)
    public KitchenDto.KitchenStats getKitchenStats() {
        List<Order> activeOrders = getActiveOrders();

        long preparing = activeOrders.stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.PREPARING)
                .count();
        long ready = activeOrders.stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.READY)
                .count();

        // Calculate average prep time from orders completed today
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        List<Order> completedToday = orderRepository.findByCreatedAtBetween(startOfDay, endOfDay)
                .stream()
                .filter(o -> o.getCompletedAt() != null)
                .toList();

        double avgPrepTime = completedToday.stream()
                .mapToLong(o -> Duration.between(o.getCreatedAt(), o.getCompletedAt()).toMinutes())
                .average()
                .orElse(0.0);

        return KitchenDto.KitchenStats.builder()
                .activeOrders(activeOrders.size())
                .preparingOrders(preparing)
                .readyOrders(ready)
                .avgPrepTimeMinutes(avgPrepTime)
                .build();
    }
}
