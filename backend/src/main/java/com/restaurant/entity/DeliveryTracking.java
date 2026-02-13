package com.restaurant.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_tracking")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class DeliveryTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private User driver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private DeliveryStatus status = DeliveryStatus.PENDING;

    @Column(length = 300)
    private String deliveryAddress;

    @Column(length = 100)
    private String city;

    @Column(length = 20)
    private String postalCode;

    @Column(length = 500)
    private String deliveryInstructions;

    @Column(length = 20)
    private String contactPhone;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal deliveryFee = BigDecimal.ZERO;

    private Integer estimatedMinutes;

    private LocalDateTime assignedAt;

    private LocalDateTime pickedUpAt;

    private LocalDateTime deliveredAt;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum DeliveryStatus {
        PENDING, ASSIGNED, PICKED_UP, IN_TRANSIT, DELIVERED, FAILED, CANCELLED
    }
}
