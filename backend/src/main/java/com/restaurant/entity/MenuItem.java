package com.restaurant.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "menu_items")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    private String imageUrl;

    @Column(nullable = false)
    @Builder.Default
    private Boolean available = true;

    private Integer preparationTimeMinutes;

    @Column(nullable = false)
    @Builder.Default
    private Boolean vegetarian = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean vegan = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean glutenFree = false;

    /** Comma-separated allergen names, e.g. "nuts,gluten,dairy" */
    @Column(length = 500)
    private String allergens;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id")
    private KitchenStation station;

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
}
