package com.fm.foodmanagementsystem.modules.product_service.resources.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OptionItemRequest(
        @NotBlank(message = "NOT_BLANK")
        String name,

        @NotNull(message = "NOT_NULL")
        @Min(value = 0, message = "MIN_INVALID")
        Double priceAdjustment
) {
}
