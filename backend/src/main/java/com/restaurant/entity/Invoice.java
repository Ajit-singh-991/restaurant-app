package com.restaurant.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false, unique = true, length = 30)
    private String invoiceNumber;

    @Column(length = 200)
    private String billToName;

    @Column(length = 100)
    private String billToContact;

    @Column(columnDefinition = "TEXT")
    private String itemsJson;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal taxAmount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, updatable = false)
    private LocalDateTime generatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_by_id")
    private User generatedBy;

    @PrePersist
    protected void onCreate() {
        if (generatedAt == null) {
            generatedAt = LocalDateTime.now();
        }
    }
}
