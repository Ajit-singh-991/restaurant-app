package com.restaurant.repository;

import com.restaurant.entity.KitchenStation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KitchenStationRepository extends JpaRepository<KitchenStation, Long> {
    List<KitchenStation> findAllByOrderByDisplayOrderAsc();
}
