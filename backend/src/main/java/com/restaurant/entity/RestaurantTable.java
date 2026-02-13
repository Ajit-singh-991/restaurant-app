package com.restaurant.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "restaurant_tables")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class RestaurantTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer tableNumber;

    @Column(nullable = false)
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TableStatus status = TableStatus.AVAILABLE;

    @Column(length = 50)
    private String section;

    public enum TableStatus {
        AVAILABLE, OCCUPIED, RESERVED, MAINTENANCE
    }
}
