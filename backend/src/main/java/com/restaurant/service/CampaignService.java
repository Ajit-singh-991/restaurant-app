package com.restaurant.service;

import com.restaurant.dto.CampaignDto.*;
import com.restaurant.entity.Campaign;
import com.restaurant.entity.Campaign.CampaignStatus;
import com.restaurant.entity.Campaign.CampaignType;
import com.restaurant.entity.User;
import com.restaurant.repository.CampaignRepository;
import com.restaurant.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final UserRepository userRepository;

    @Transactional
    public CampaignResponse createCampaign(CreateCampaignRequest request, Long userId) {
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (request.getPromoCode() != null) {
            campaignRepository.findByPromoCode(request.getPromoCode())
                    .ifPresent(c -> { throw new IllegalArgumentException("Promo code already exists"); });
        }

        Campaign campaign = Campaign.builder()
                .name(request.getName())
                .description(request.getDescription())
                .type(CampaignType.valueOf(request.getType()))
                .discountPercent(request.getDiscountPercent())
                .discountAmount(request.getDiscountAmount())
                .promoCode(request.getPromoCode())
                .startDate(request.getStartDate() != null ? LocalDateTime.parse(request.getStartDate()) : null)
                .endDate(request.getEndDate() != null ? LocalDateTime.parse(request.getEndDate()) : null)
                .targetSegment(request.getTargetSegment())
                .maxUsage(request.getMaxUsage())
                .createdBy(creator)
                .build();

        return toResponse(campaignRepository.save(campaign));
    }

    @Transactional(readOnly = true)
    public List<CampaignResponse> getAllCampaigns() {
        return campaignRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CampaignResponse> getActiveCampaigns() {
        return campaignRepository.findActiveCampaigns(LocalDateTime.now()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CampaignResponse updateStatus(Long id, String status) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));
        campaign.setStatus(CampaignStatus.valueOf(status));
        return toResponse(campaignRepository.save(campaign));
    }

    @Transactional
    public ValidatePromoResponse validatePromoCode(String code) {
        return campaignRepository.findByPromoCode(code)
                .map(campaign -> {
                    if (campaign.getStatus() != CampaignStatus.ACTIVE) {
                        return ValidatePromoResponse.builder().valid(false).message("Campaign is not active").build();
                    }
                    if (campaign.getEndDate() != null && campaign.getEndDate().isBefore(LocalDateTime.now())) {
                        return ValidatePromoResponse.builder().valid(false).message("Promo code has expired").build();
                    }
                    if (campaign.getMaxUsage() != null && campaign.getUsageCount() >= campaign.getMaxUsage()) {
                        return ValidatePromoResponse.builder().valid(false).message("Promo code usage limit reached").build();
                    }
                    return ValidatePromoResponse.builder()
                            .valid(true)
                            .message("Promo code is valid")
                            .discountPercent(campaign.getDiscountPercent())
                            .discountAmount(campaign.getDiscountAmount())
                            .type(campaign.getType().name())
                            .build();
                })
                .orElse(ValidatePromoResponse.builder().valid(false).message("Invalid promo code").build());
    }

    @Transactional
    public ValidatePromoResponse redeemPromoCode(String code) {
        ValidatePromoResponse validation = validatePromoCode(code);
        if (validation.isValid()) {
            Campaign campaign = campaignRepository.findByPromoCode(code).orElseThrow();
            campaign.setUsageCount(campaign.getUsageCount() + 1);
            campaignRepository.save(campaign);
        }
        return validation;
    }

    @Transactional(readOnly = true)
    public CampaignStats getCampaignStats() {
        List<Campaign> all = campaignRepository.findAll();
        long active = campaignRepository.findActiveCampaigns(LocalDateTime.now()).size();
        long totalRedemptions = all.stream().mapToInt(Campaign::getUsageCount).sum();

        List<CampaignResponse> topCampaigns = all.stream()
                .sorted(Comparator.comparingInt(Campaign::getUsageCount).reversed())
                .limit(5)
                .map(this::toResponse)
                .collect(Collectors.toList());

        return CampaignStats.builder()
                .totalCampaigns(all.size())
                .activeCampaigns(active)
                .totalRedemptions(totalRedemptions)
                .topCampaigns(topCampaigns)
                .build();
    }

    private CampaignResponse toResponse(Campaign c) {
        return CampaignResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .description(c.getDescription())
                .type(c.getType().name())
                .status(c.getStatus().name())
                .discountPercent(c.getDiscountPercent())
                .discountAmount(c.getDiscountAmount())
                .promoCode(c.getPromoCode())
                .startDate(c.getStartDate() != null ? c.getStartDate().toString() : null)
                .endDate(c.getEndDate() != null ? c.getEndDate().toString() : null)
                .targetSegment(c.getTargetSegment())
                .usageCount(c.getUsageCount())
                .maxUsage(c.getMaxUsage())
                .createdBy(c.getCreatedBy() != null ? c.getCreatedBy().getFullName() : null)
                .createdAt(c.getCreatedAt() != null ? c.getCreatedAt().toString() : null)
                .build();
    }
}
