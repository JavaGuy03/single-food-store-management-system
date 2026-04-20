package com.fm.foodmanagementsystem.modules.order_service.resources.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderItemRequest(
        @NotNull(message = "NOT_NULL")
        Long foodId,

        @NotNull(message = "NOT_NULL")
        @Min(value = 1, message = "INVALID_MIN")
        Integer quantity,

        List<Long> selectedOptionIds
) {}