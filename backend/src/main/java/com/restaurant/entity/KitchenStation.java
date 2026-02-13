package com.restaurant.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "kitchen_stations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KitchenStation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;
}
