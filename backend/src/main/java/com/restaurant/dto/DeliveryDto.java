package com.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

public class DeliveryDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateDeliveryRequest {
        private Long orderId;
        private String deliveryAddress;
        private String city;
        private String postalCode;
        private String deliveryInstructions;
        private String contactPhone;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryResponse {
        private Long id;
        private Long orderId;
        private String orderNumber;
        private Long driverId;
        private String driverName;
        private String status;
        private String deliveryAddress;
        private String city;
        private String postalCode;
        private String deliveryInstructions;
        private String contactPhone;
        private BigDecimal deliveryFee;
        private Integer estimatedMinutes;
        private String assignedAt;
        private String pickedUpAt;
        private String deliveredAt;
        private String createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssignDriverRequest {
        private Long driverId;
        private Integer estimatedMinutes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryStats {
        private long totalDeliveries;
        private long activeDeliveries;
        private long completedToday;
        private double avgDeliveryTimeMinutes;
        private List<DriverPerformance> driverPerformance;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DriverPerformance {
        private Long driverId;
        private String driverName;
        private long deliveriesCompleted;
        private double avgDeliveryTimeMinutes;
        private long activeDeliveries;
    }
}
