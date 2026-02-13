package com.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class KitchenDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KitchenStats {
        private long activeOrders;
        private long preparingOrders;
        private long readyOrders;
        private double avgPrepTimeMinutes;
    }
}
