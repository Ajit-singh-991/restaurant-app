package com.restaurant.service;

import com.restaurant.dto.DeliveryDto.*;
import com.restaurant.entity.DeliveryTracking;
import com.restaurant.entity.DeliveryTracking.DeliveryStatus;
import com.restaurant.entity.Order;
import com.restaurant.entity.User;
import com.restaurant.repository.DeliveryTrackingRepository;
import com.restaurant.repository.OrderRepository;
import com.restaurant.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryTrackingRepository deliveryRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    private static final BigDecimal DEFAULT_DELIVERY_FEE = new BigDecimal("5.00");

    @Transactional
    public DeliveryResponse createDelivery(CreateDeliveryRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (order.getOrderType() != Order.OrderType.DELIVERY) {
            throw new IllegalArgumentException("Order is not a delivery order");
        }

        deliveryRepository.findByOrderId(request.getOrderId())
                .ifPresent(d -> { throw new IllegalArgumentException("Delivery tracking already exists for this order"); });

        DeliveryTracking delivery = DeliveryTracking.builder()
                .order(order)
                .deliveryAddress(request.getDeliveryAddress())
                .city(request.getCity())
                .postalCode(request.getPostalCode())
                .deliveryInstructions(request.getDeliveryInstructions())
                .contactPhone(request.getContactPhone())
                .deliveryFee(DEFAULT_DELIVERY_FEE)
                .build();

        return toResponse(deliveryRepository.save(delivery));
    }

    @Transactional
    public DeliveryResponse assignDriver(Long deliveryId, Long driverId, Integer estimatedMinutes) {
        DeliveryTracking delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException("Delivery not found"));

        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found"));

        delivery.setDriver(driver);
        delivery.setStatus(DeliveryStatus.ASSIGNED);
        delivery.setAssignedAt(LocalDateTime.now());
        delivery.setEstimatedMinutes(estimatedMinutes);

        DeliveryTracking saved = deliveryRepository.save(delivery);
        notificationService.notifyOrderStatusChanged(delivery.getOrder());
        return toResponse(saved);
    }

    @Transactional
    public DeliveryResponse updateStatus(Long deliveryId, DeliveryStatus status) {
        DeliveryTracking delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException("Delivery not found"));

        delivery.setStatus(status);

        if (status == DeliveryStatus.PICKED_UP) {
            delivery.setPickedUpAt(LocalDateTime.now());
        } else if (status == DeliveryStatus.DELIVERED) {
            delivery.setDeliveredAt(LocalDateTime.now());
            delivery.getOrder().setStatus(Order.OrderStatus.COMPLETED);
            delivery.getOrder().setCompletedAt(LocalDateTime.now());
            orderRepository.save(delivery.getOrder());
        } else if (status == DeliveryStatus.CANCELLED || status == DeliveryStatus.FAILED) {
            delivery.getOrder().setStatus(Order.OrderStatus.CANCELLED);
            orderRepository.save(delivery.getOrder());
        }

        DeliveryTracking saved = deliveryRepository.save(delivery);
        notificationService.notifyOrderStatusChanged(delivery.getOrder());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public DeliveryResponse getByOrderId(Long orderId) {
        DeliveryTracking delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Delivery not found for this order"));
        return toResponse(delivery);
    }

    @Transactional(readOnly = true)
    public List<DeliveryResponse> getActiveDeliveries() {
        return deliveryRepository.findActiveDeliveries().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DeliveryResponse> getPendingDeliveries() {
        return deliveryRepository.findByStatusOrderByCreatedAtAsc(DeliveryStatus.PENDING).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DeliveryResponse> getDriverDeliveries(Long driverId) {
        return deliveryRepository.findByDriverIdOrderByCreatedAtDesc(driverId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DeliveryStats getDeliveryStats() {
        List<DeliveryTracking> all = deliveryRepository.findAll();
        List<DeliveryTracking> active = deliveryRepository.findActiveDeliveries();

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        List<DeliveryTracking> completedToday = all.stream()
                .filter(d -> d.getStatus() == DeliveryStatus.DELIVERED)
                .filter(d -> d.getDeliveredAt() != null && d.getDeliveredAt().isAfter(todayStart))
                .collect(Collectors.toList());

        double avgTime = completedToday.stream()
                .filter(d -> d.getAssignedAt() != null && d.getDeliveredAt() != null)
                .mapToLong(d -> Duration.between(d.getAssignedAt(), d.getDeliveredAt()).toMinutes())
                .average()
                .orElse(0.0);

        List<DriverPerformance> driverPerf = all.stream()
                .filter(d -> d.getDriver() != null)
                .collect(Collectors.groupingBy(d -> d.getDriver().getId()))
                .entrySet().stream()
                .map(entry -> {
                    List<DeliveryTracking> driverDeliveries = entry.getValue();
                    User driver = driverDeliveries.get(0).getDriver();
                    long completed = driverDeliveries.stream()
                            .filter(d -> d.getStatus() == DeliveryStatus.DELIVERED).count();
                    long activeCount = driverDeliveries.stream()
                            .filter(d -> d.getStatus() != DeliveryStatus.DELIVERED
                                    && d.getStatus() != DeliveryStatus.FAILED
                                    && d.getStatus() != DeliveryStatus.CANCELLED).count();
                    double driverAvgTime = driverDeliveries.stream()
                            .filter(d -> d.getAssignedAt() != null && d.getDeliveredAt() != null)
                            .mapToLong(d -> Duration.between(d.getAssignedAt(), d.getDeliveredAt()).toMinutes())
                            .average().orElse(0.0);

                    return DriverPerformance.builder()
                            .driverId(driver.getId())
                            .driverName(driver.getFullName())
                            .deliveriesCompleted(completed)
                            .avgDeliveryTimeMinutes(Math.round(driverAvgTime * 10.0) / 10.0)
                            .activeDeliveries(activeCount)
                            .build();
                })
                .collect(Collectors.toList());

        return DeliveryStats.builder()
                .totalDeliveries(all.size())
                .activeDeliveries(active.size())
                .completedToday(completedToday.size())
                .avgDeliveryTimeMinutes(Math.round(avgTime * 10.0) / 10.0)
                .driverPerformance(driverPerf)
                .build();
    }

    private DeliveryResponse toResponse(DeliveryTracking d) {
        return DeliveryResponse.builder()
                .id(d.getId())
                .orderId(d.getOrder().getId())
                .orderNumber(d.getOrder().getOrderNumber())
                .driverId(d.getDriver() != null ? d.getDriver().getId() : null)
                .driverName(d.getDriver() != null ? d.getDriver().getFullName() : null)
                .status(d.getStatus().name())
                .deliveryAddress(d.getDeliveryAddress())
                .city(d.getCity())
                .postalCode(d.getPostalCode())
                .deliveryInstructions(d.getDeliveryInstructions())
                .contactPhone(d.getContactPhone())
                .deliveryFee(d.getDeliveryFee())
                .estimatedMinutes(d.getEstimatedMinutes())
                .assignedAt(d.getAssignedAt() != null ? d.getAssignedAt().toString() : null)
                .pickedUpAt(d.getPickedUpAt() != null ? d.getPickedUpAt().toString() : null)
                .deliveredAt(d.getDeliveredAt() != null ? d.getDeliveredAt().toString() : null)
                .createdAt(d.getCreatedAt() != null ? d.getCreatedAt().toString() : null)
                .build();
    }
}
