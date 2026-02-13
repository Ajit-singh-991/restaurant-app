package com.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

public class StaffDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeEntryResponse {
        private Long id;
        private Long staffId;
        private String staffName;
        private String role;
        private String clockIn;
        private String clockOut;
        private BigDecimal hoursWorked;
        private BigDecimal tips;
        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddTipRequest {
        private BigDecimal amount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StaffPerformance {
        private Long staffId;
        private String staffName;
        private String role;
        private BigDecimal totalHoursWorked;
        private BigDecimal totalTips;
        private long ordersHandled;
        private int shiftsCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StaffSummary {
        private int activeSessions;
        private BigDecimal totalHoursToday;
        private BigDecimal totalTipsToday;
        private List<StaffPerformance> staffPerformance;
    }
}
