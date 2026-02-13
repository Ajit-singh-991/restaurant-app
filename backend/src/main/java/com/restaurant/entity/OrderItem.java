package com.restaurant.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(length = 300)
    private String specialRequests;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ItemStatus status = ItemStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id")
    private KitchenStation station;

    @PrePersist
    @PreUpdate
    protected void calculateTotal() {
        if (unitPrice != null && quantity != null) {
            totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public enum ItemStatus {
        PENDING, PREPARING, READY, SERVED, CANCELLED
    }
}
