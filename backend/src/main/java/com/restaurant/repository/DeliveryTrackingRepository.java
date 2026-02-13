package com.restaurant.repository;

import com.restaurant.entity.DeliveryTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryTrackingRepository extends JpaRepository<DeliveryTracking, Long> {

    Optional<DeliveryTracking> findByOrderId(Long orderId);

    List<DeliveryTracking> findByDriverIdAndStatusIn(Long driverId, List<DeliveryTracking.DeliveryStatus> statuses);

    List<DeliveryTracking> findByStatusOrderByCreatedAtAsc(DeliveryTracking.DeliveryStatus status);

    @Query("SELECT d FROM DeliveryTracking d WHERE d.status NOT IN ('DELIVERED', 'FAILED', 'CANCELLED') ORDER BY d.createdAt ASC")
    List<DeliveryTracking> findActiveDeliveries();

    List<DeliveryTracking> findByDriverIdOrderByCreatedAtDesc(Long driverId);
}
