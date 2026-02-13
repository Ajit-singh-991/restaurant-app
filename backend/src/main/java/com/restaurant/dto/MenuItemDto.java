package com.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MenuItemDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Long categoryId;
    private String categoryName;
    private String imageUrl;
    private Boolean available;
    private Integer preparationTimeMinutes;
    private Boolean vegetarian;
    private Boolean vegan;
    private Boolean glutenFree;
    private String allergens;
    private Long stationId;

    @Data
    public static class CreateRequest {
        @NotBlank
        private String name;

        private String description;

        @NotNull
        @Positive
        private BigDecimal price;

        @NotNull
        private Long categoryId;

        private String imageUrl;
        private Integer preparationTimeMinutes;
        private Boolean vegetarian;
        private Boolean vegan;
        private Boolean glutenFree;
        private String allergens;
        private Long stationId;
    }
}
