package com.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class LoyaltyDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoyaltyAccountResponse {
        private Long id;
        private Long customerId;
        private String customerName;
        private int points;
        private int totalPointsEarned;
        private String tier;
        private int pointsToNextTier;
        private String createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoyaltyTransactionResponse {
        private Long id;
        private String type;
        private int points;
        private String description;
        private Long orderId;
        private String createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RedeemRequest {
        private int points;
    }
}
