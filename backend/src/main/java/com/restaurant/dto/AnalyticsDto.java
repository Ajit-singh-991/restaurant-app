package com.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class AnalyticsDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardStats {
        private long totalOrders;
        private BigDecimal totalRevenue;
        private BigDecimal averageOrderValue;
        private long activeOrders;
        private int tablesOccupied;
        private int totalTables;
        private Map<String, Long> ordersByStatus;
        private Map<String, Long> ordersByType;
        private Map<String, Long> ordersByPaymentMethod;
        private List<TopItem> topSellingItems;
        private List<RecentOrderSummary> recentOrders;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentOrderSummary {
        private Long id;
        private String orderNumber;
        private String status;
        private BigDecimal totalAmount;
        private java.time.LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopItem {
        private Long itemId;
        private String itemName;
        private long quantitySold;
        private BigDecimal revenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesReport {
        private List<DailySales> dailySales;
        private List<CategoryRevenue> revenueByCategory;
        private BigDecimal totalRevenue;
        private long totalOrders;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailySales {
        private String date;
        private long orders;
        private BigDecimal revenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryRevenue {
        private String category;
        private BigDecimal revenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MenuAnalytics {
        private long totalItems;
        private long availableItems;
        private List<TopItem> topSelling;
        private List<TopItem> bottomSelling;
        private List<CategoryRevenue> revenueByCategory;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerAnalytics {
        private long totalCustomers;
        private long newCustomersThisMonth;
        private long repeatCustomers;
        private double averageOrdersPerCustomer;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StaffPerformanceEntry {
        private Long staffId;
        private String staffName;
        private long ordersHandled;
        private BigDecimal totalTips;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StaffPerformance {
        private List<StaffPerformanceEntry> entries;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventoryReport {
        private List<InventoryItem> lowStockItems;
        private BigDecimal totalStockValue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventoryItem {
        private Long id;
        private String name;
        private String unit;
        private BigDecimal currentStock;
        private BigDecimal reorderLevel;
    }

    public enum ReportType {
        SALES, MENU, CUSTOMERS, STAFF, INVENTORY
    }

    public enum ExportFormat {
        PDF, CSV, EXCEL
    }
}
