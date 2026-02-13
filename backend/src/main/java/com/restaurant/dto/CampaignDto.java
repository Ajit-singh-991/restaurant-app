package com.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

public class CampaignDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateCampaignRequest {
        private String name;
        private String description;
        private String type;
        private BigDecimal discountPercent;
        private BigDecimal discountAmount;
        private String promoCode;
        private String startDate;
        private String endDate;
        private String targetSegment;
        private Integer maxUsage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CampaignResponse {
        private Long id;
        private String name;
        private String description;
        private String type;
        private String status;
        private BigDecimal discountPercent;
        private BigDecimal discountAmount;
        private String promoCode;
        private String startDate;
        private String endDate;
        private String targetSegment;
        private Integer usageCount;
        private Integer maxUsage;
        private String createdBy;
        private String createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidatePromoResponse {
        private boolean valid;
        private String message;
        private BigDecimal discountPercent;
        private BigDecimal discountAmount;
        private String type;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CampaignStats {
        private long totalCampaigns;
        private long activeCampaigns;
        private long totalRedemptions;
        private List<CampaignResponse> topCampaigns;
    }
}
