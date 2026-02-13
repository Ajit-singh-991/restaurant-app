package com.restaurant.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "campaigns")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CampaignType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private CampaignStatus status = CampaignStatus.DRAFT;

    @Column(precision = 5, scale = 2)
    private BigDecimal discountPercent;

    @Column(precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @Column(length = 50, unique = true)
    private String promoCode;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @Column(length = 50)
    private String targetSegment;

    @Builder.Default
    private Integer usageCount = 0;

    private Integer maxUsage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

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

    public enum CampaignType {
        PERCENTAGE_DISCOUNT, FLAT_DISCOUNT, BUY_ONE_GET_ONE, FREE_DELIVERY, HAPPY_HOUR
    }

    public enum CampaignStatus {
        DRAFT, ACTIVE, PAUSED, EXPIRED, CANCELLED
    }
}
