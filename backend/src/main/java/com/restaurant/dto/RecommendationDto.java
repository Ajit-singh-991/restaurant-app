package com.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

public class RecommendationDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendedItem {
        private Long itemId;
        private String itemName;
        private String category;
        private BigDecimal price;
        private double score;
        private String reason;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PersonalizedRecommendations {
        private List<RecommendedItem> recommendations;
        private String strategy;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendingItems {
        private List<RecommendedItem> items;
        private String period;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PairingSuggestion {
        private Long itemId;
        private String itemName;
        private List<RecommendedItem> pairsWith;
    }
}
