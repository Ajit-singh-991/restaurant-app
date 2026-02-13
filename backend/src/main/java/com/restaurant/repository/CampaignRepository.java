package com.restaurant.repository;

import com.restaurant.entity.Campaign;
import com.restaurant.entity.Campaign.CampaignStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    List<Campaign> findByStatus(CampaignStatus status);

    Optional<Campaign> findByPromoCode(String promoCode);

    @Query("SELECT c FROM Campaign c WHERE c.status = 'ACTIVE' AND c.startDate <= :now AND (c.endDate IS NULL OR c.endDate >= :now)")
    List<Campaign> findActiveCampaigns(@Param("now") LocalDateTime now);

    List<Campaign> findByTargetSegment(String segment);
}
