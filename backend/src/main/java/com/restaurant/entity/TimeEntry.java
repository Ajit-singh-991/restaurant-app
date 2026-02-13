package com.restaurant.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "time_entries")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TimeEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private User staff;

    @Column(nullable = false)
    private LocalDateTime clockIn;

    private LocalDateTime clockOut;

    @Column(precision = 10, scale = 2)
    private BigDecimal hoursWorked;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal tips = BigDecimal.ZERO;

    @Column(length = 200)
    private String notes;

    @PrePersist
    protected void onCreate() {
        if (clockIn == null) {
            clockIn = LocalDateTime.now();
        }
    }

    public void clockOut() {
        this.clockOut = LocalDateTime.now();
        long minutes = java.time.Duration.between(clockIn, this.clockOut).toMinutes();
        this.hoursWorked = BigDecimal.valueOf(minutes / 60.0).setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
