package com.restaurant.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentDto {
    private Long id;
    private Long orderId;
    private String orderNumber;
    private BigDecimal amount;
    private String paymentMethod;
    private String transactionId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime refundedAt;
    private String refundReason;

    @Data
    public static class PaymentRequest {
        @NotNull
        private Long orderId;

        @NotNull
        private String paymentMethod;

        private String transactionId;
        /** Optional: send receipt PDF to this email when app.mail.enabled is true */
        private String recipientEmail;
    }

    @Data
    public static class RefundRequest {
        private String reason;
    }

    @Data
    public static class PaymentStats {
        private BigDecimal dailySales;
        private long dailyTransactions;
        private BigDecimal weeklySales;
        private long weeklyTransactions;
        private BigDecimal averageOrderValue;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailySalesResponse {
        private BigDecimal amount;
    }
}
