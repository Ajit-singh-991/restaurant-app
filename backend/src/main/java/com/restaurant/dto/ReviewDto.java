package com.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class ReviewDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateReviewRequest {
        private Long orderId;
        private Long menuItemId;
        private Integer rating;
        private String comment;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewResponse {
        private Long id;
        private Long customerId;
        private String customerName;
        private Long orderId;
        private Long menuItemId;
        private String menuItemName;
        private Integer rating;
        private String comment;
        private String managementResponse;
        private String respondedAt;
        private String createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RespondRequest {
        private String response;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemRatingSummary {
        private Long menuItemId;
        private String menuItemName;
        private double averageRating;
        private long totalReviews;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewStats {
        private double overallRating;
        private long totalReviews;
        private long unansweredCount;
        private List<RatingBreakdown> ratingBreakdown;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RatingBreakdown {
        private int rating;
        private long count;
        private double percentage;
    }
}
