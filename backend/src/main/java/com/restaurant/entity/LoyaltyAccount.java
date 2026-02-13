package com.restaurant.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "loyalty_accounts")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class LoyaltyAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false, unique = true)
    private User customer;

    @Column(nullable = false)
    @Builder.Default
    private Integer points = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalPointsEarned = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Tier tier = Tier.BRONZE;

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

    public enum Tier {
        BRONZE, SILVER, GOLD, PLATINUM
    }
}
