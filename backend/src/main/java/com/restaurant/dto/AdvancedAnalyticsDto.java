package com.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

public class AdvancedAnalyticsDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesForecast {
        private List<ForecastPoint> forecast;
        private BigDecimal projectedWeeklyRevenue;
        private BigDecimal projectedMonthlyRevenue;
        private double growthTrendPercent;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ForecastPoint {
        private String date;
        private BigDecimal predictedRevenue;
        private long predictedOrders;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerSegment {
        private String segment;
        private long customerCount;
        private BigDecimal avgOrderValue;
        private double avgOrderFrequency;
        private BigDecimal totalRevenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerSegmentation {
        private List<CustomerSegment> segments;
        private long totalCustomers;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MenuOptimization {
        private List<MenuSuggestion> suggestions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MenuSuggestion {
        private Long itemId;
        private String itemName;
        private String suggestion;
        private String reason;
        private String priority;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PeakHoursAnalysis {
        private List<HourlyData> hourlyData;
        private int peakHour;
        private int slowestHour;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HourlyData {
        private int hour;
        private long orderCount;
        private BigDecimal revenue;
    }
}
