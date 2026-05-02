package com.fm.foodmanagementsystem.modules.interaction_service.resources.requests;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReviewRequest(
        @NotBlank(message = "NOT_BLANK")
        String orderId,

        // C-6: Now required — specifies which food item in the order is being rated
        @NotNull(message = "NOT_NULL")
        Long foodId,

        @NotNull(message = "NOT_NULL")
        @Min(value = 1, message = "INVALID_MIN")
        @Max(value = 5, message = "INVALID_MAX")
        Integer rating,

        String comment
) {
}
