package com.restaurant.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDto {
    private Long id;
    private String orderNumber;
    private Long tableId;
    private Integer tableNumber;
    private Long waiterId;
    private String waiterName;
    private String status;
    private String orderType;
    private List<OrderItemDto> items;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String specialInstructions;
    private LocalDateTime createdAt;

    @Data
    public static class CreateRequest {
        @NotNull
        private Long tableId;

        @NotEmpty
        private List<OrderItemRequest> items;

        private String orderType;
        private String specialInstructions;
    }

    @Data
    public static class OrderItemRequest {
        @NotNull
        private Long menuItemId;

        @NotNull
        private Integer quantity;

        private String specialRequests;
    }

    @Data
    public static class OrderItemDto {
        private Long id;
        private Long menuItemId;
        private String menuItemName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private String specialRequests;
        private String status;
    }

    @Data
    public static class OrderStats {
        private long dailyOrders;
        private BigDecimal dailyRevenue;
        private long weeklyOrders;
        private BigDecimal weeklyRevenue;
        private long monthlyOrders;
        private BigDecimal monthlyRevenue;
    }

    @Data
    public static class CancelRequest {
        private String reason;
    }

    @Data
    public static class SplitRequest {
        @Min(2)
        private Integer numberOfWays;
        /** Percentages per person (must sum to 100). Used when non-null and non-empty. */
        private List<java.math.BigDecimal> percentages;
        /** For custom split: each list is order item ids for that person. */
        private List<List<Long>> personItemIds;
    }

    @Data
    public static class SplitResponse {
        private BigDecimal orderTotal;
        private List<BigDecimal> amountsPerPerson;
    }
}
