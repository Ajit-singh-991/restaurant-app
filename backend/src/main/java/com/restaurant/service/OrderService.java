package com.restaurant.service;

import com.restaurant.dto.OrderDto;
import com.restaurant.entity.*;
import com.restaurant.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final TableRepository tableRepository;
    private final NotificationService notificationService;

    private static final BigDecimal TAX_RATE = new BigDecimal("0.10");
    private final AtomicLong orderCounter = new AtomicLong(0);

    @Transactional
    public Order createOrder(OrderDto.CreateRequest request, User waiter, User customer) {
        RestaurantTable table = tableRepository.findById(request.getTableId())
                .orElseThrow(() -> new IllegalArgumentException("Table not found"));

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .table(table)
                .waiter(waiter)
                .customer(customer)
                .orderType(Order.OrderType.valueOf(
                        request.getOrderType() != null ? request.getOrderType() : "DINE_IN"))
                .specialInstructions(request.getSpecialInstructions())
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderDto.OrderItemRequest itemReq : request.getItems()) {
            MenuItem menuItem = menuItemRepository.findById(itemReq.getMenuItemId())
                    .orElseThrow(() -> new IllegalArgumentException("Menu item not found: " + itemReq.getMenuItemId()));

            if (!menuItem.getAvailable()) {
                throw new IllegalArgumentException("Menu item not available: " + menuItem.getName());
            }

            OrderItem orderItem = OrderItem.builder()
                    .menuItem(menuItem)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(menuItem.getPrice())
                    .specialRequests(itemReq.getSpecialRequests())
                    .station(menuItem.getStation())
                    .build();

            order.addItem(orderItem);
            subtotal = subtotal.add(menuItem.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())));
        }

        order.setSubtotal(subtotal);
        order.setTaxAmount(subtotal.multiply(TAX_RATE));
        order.setTotalAmount(subtotal.add(order.getTaxAmount()));

        table.setStatus(RestaurantTable.TableStatus.OCCUPIED);
        tableRepository.save(table);

        Order saved = orderRepository.save(order);
        notificationService.notifyOrderCreated(saved);
        return saved;
    }

    @Transactional(readOnly = true)
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
    }

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
    public List<Order> getOrdersByStatus(Order.OrderStatus status) {
        return orderRepository.findByStatusIn(List.of(status));
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, Order.OrderStatus newStatus) {
        Order order = getOrderById(orderId);
        order.setStatus(newStatus);

        if (newStatus == Order.OrderStatus.COMPLETED || newStatus == Order.OrderStatus.CANCELLED) {
            order.setCompletedAt(LocalDateTime.now());
            if (order.getTable() != null) {
                order.getTable().setStatus(RestaurantTable.TableStatus.AVAILABLE);
                tableRepository.save(order.getTable());
            }
        }

        Order updated = orderRepository.save(order);
        notificationService.notifyOrderStatusChanged(updated);
        return updated;
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByDateRange(LocalDateTime start, LocalDateTime end) {
        return orderRepository.findByCreatedAtBetween(start, end);
    }

    @Transactional(readOnly = true)
    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByCustomerId(Long customerId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    @Transactional(readOnly = true)
    public List<Order> getActiveTableOrders(Long tableId) {
        return orderRepository.findByTableIdAndStatusIn(tableId, List.of(
                Order.OrderStatus.PENDING,
                Order.OrderStatus.CONFIRMED,
                Order.OrderStatus.PREPARING,
                Order.OrderStatus.READY
        ));
    }

    @Transactional(readOnly = true)
    public OrderDto.OrderStats getOrderStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dayStart = LocalDate.now().atStartOfDay();
        LocalDateTime dayEnd = LocalDate.now().atTime(LocalTime.MAX);
        LocalDateTime weekStart = now.minusDays(7);
        LocalDateTime monthStart = now.minusDays(30);

        long dailyOrders = orderRepository.countByCreatedAtBetween(dayStart, dayEnd);
        BigDecimal dailyRevenue = orderRepository.sumRevenueBetween(dayStart, dayEnd);
        long weeklyOrders = orderRepository.countByCreatedAtBetween(weekStart, now);
        BigDecimal weeklyRevenue = orderRepository.sumRevenueBetween(weekStart, now);
        long monthlyOrders = orderRepository.countByCreatedAtBetween(monthStart, now);
        BigDecimal monthlyRevenue = orderRepository.sumRevenueBetween(monthStart, now);

        OrderDto.OrderStats stats = new OrderDto.OrderStats();
        stats.setDailyOrders(dailyOrders);
        stats.setDailyRevenue(dailyRevenue != null ? dailyRevenue : BigDecimal.ZERO);
        stats.setWeeklyOrders(weeklyOrders);
        stats.setWeeklyRevenue(weeklyRevenue != null ? weeklyRevenue : BigDecimal.ZERO);
        stats.setMonthlyOrders(monthlyOrders);
        stats.setMonthlyRevenue(monthlyRevenue != null ? monthlyRevenue : BigDecimal.ZERO);
        return stats;
    }

    @Transactional
    public Order updateOrderItems(Long orderId, List<OrderDto.OrderItemRequest> itemRequests) {
        Order order = getOrderById(orderId);
        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new IllegalArgumentException("Can only update items for PENDING orders");
        }
        order.getItems().clear();
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderDto.OrderItemRequest itemReq : itemRequests) {
            MenuItem menuItem = menuItemRepository.findById(itemReq.getMenuItemId())
                    .orElseThrow(() -> new IllegalArgumentException("Menu item not found: " + itemReq.getMenuItemId()));
            if (!menuItem.getAvailable()) {
                throw new IllegalArgumentException("Menu item not available: " + menuItem.getName());
            }
            OrderItem orderItem = OrderItem.builder()
                    .menuItem(menuItem)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(menuItem.getPrice())
                    .specialRequests(itemReq.getSpecialRequests())
                    .station(menuItem.getStation())
                    .build();
            order.addItem(orderItem);
            subtotal = subtotal.add(menuItem.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())));
        }
        order.setSubtotal(subtotal);
        order.setTaxAmount(subtotal.multiply(TAX_RATE));
        order.setTotalAmount(subtotal.add(order.getTaxAmount()));
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public OrderDto.SplitResponse calculateSplit(Long orderId, OrderDto.SplitRequest request) {
        Order order = getOrderById(orderId);
        if (order.getTotalAmount() == null || order.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Order has no amount to split");
        }
        BigDecimal total = order.getTotalAmount();
        List<BigDecimal> amounts;

        if (request.getPersonItemIds() != null && !request.getPersonItemIds().isEmpty()) {
            // Custom split by assigned items
            List<Long> allItemIds = order.getItems().stream().map(oi -> oi.getId()).toList();
            BigDecimal subtotal = order.getItems().stream()
                    .map(oi -> oi.getTotalPrice() != null ? oi.getTotalPrice() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal taxRate = subtotal.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                    : order.getTaxAmount().divide(subtotal, 4, java.math.RoundingMode.HALF_UP);
            amounts = new java.util.ArrayList<>();
            for (List<Long> itemIds : request.getPersonItemIds()) {
                BigDecimal personSub = order.getItems().stream()
                        .filter(oi -> itemIds.contains(oi.getId()))
                        .map(oi -> oi.getTotalPrice() != null ? oi.getTotalPrice() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal personTax = personSub.multiply(taxRate).setScale(2, java.math.RoundingMode.HALF_UP);
                amounts.add(personSub.add(personTax));
            }
        } else if (request.getPercentages() != null && !request.getPercentages().isEmpty()) {
            BigDecimal sum = request.getPercentages().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            if (sum.compareTo(new BigDecimal("100")) != 0) {
                throw new IllegalArgumentException("Percentages must sum to 100");
            }
            amounts = request.getPercentages().stream()
                    .map(p -> total.multiply(p).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP))
                    .toList();
        } else {
            int n = request.getNumberOfWays() != null && request.getNumberOfWays() >= 2
                    ? request.getNumberOfWays() : 2;
            BigDecimal perPerson = total.divide(BigDecimal.valueOf(n), 2, java.math.RoundingMode.HALF_UP);
            amounts = java.util.Collections.nCopies(n, perPerson);
        }

        OrderDto.SplitResponse response = new OrderDto.SplitResponse();
        response.setOrderTotal(total);
        response.setAmountsPerPerson(amounts);
        return response;
    }

    @Transactional
    public Order cancelOrder(Long orderId, String reason) {
        Order order = getOrderById(orderId);
        if (order.getStatus() == Order.OrderStatus.COMPLETED || order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("Order cannot be cancelled");
        }
        order.setStatus(Order.OrderStatus.CANCELLED);
        order.setCompletedAt(LocalDateTime.now());
        if (order.getTable() != null) {
            order.getTable().setStatus(RestaurantTable.TableStatus.AVAILABLE);
            tableRepository.save(order.getTable());
        }
        Order updated = orderRepository.save(order);
        notificationService.notifyOrderStatusChanged(updated);
        return updated;
    }

    private String generateOrderNumber() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = orderCounter.incrementAndGet();
        return String.format("ORD-%s-%04d", date, count);
    }
}
