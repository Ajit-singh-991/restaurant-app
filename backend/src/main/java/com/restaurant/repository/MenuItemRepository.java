package com.restaurant.repository;

import com.restaurant.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByCategoryIdAndAvailableTrue(Long categoryId);
    List<MenuItem> findByAvailableTrue();
    List<MenuItem> findByNameContainingIgnoreCase(String name);
}
