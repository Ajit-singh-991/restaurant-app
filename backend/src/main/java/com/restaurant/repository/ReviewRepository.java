package com.restaurant.repository;

import com.restaurant.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByMenuItemIdOrderByCreatedAtDesc(Long menuItemId);

    List<Review> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<Review> findByOrderId(Long orderId);

    Optional<Review> findByCustomerIdAndOrderId(Long customerId, Long orderId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.menuItem.id = :menuItemId")
    Double findAverageRatingByMenuItemId(@Param("menuItemId") Long menuItemId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.menuItem.id = :menuItemId")
    Long countByMenuItemId(@Param("menuItemId") Long menuItemId);

    @Query("SELECT AVG(r.rating) FROM Review r")
    Double findOverallAverageRating();

    @Query("SELECT r FROM Review r WHERE r.managementResponse IS NULL ORDER BY r.createdAt DESC")
    List<Review> findUnansweredReviews();

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.menuItem.id = :menuItemId")
    Double getAverageRatingForItem(@Param("menuItemId") Long menuItemId);
}
