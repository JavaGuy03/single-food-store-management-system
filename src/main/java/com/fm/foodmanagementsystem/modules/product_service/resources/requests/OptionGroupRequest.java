package com.fm.foodmanagementsystem.modules.product_service.resources.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OptionGroupRequest(
        @NotBlank(message = "NOT_BLANK")
        String name,

        @NotNull(message = "NOT_NULL")
        @Min(value = 0, message = "INVALID_MIN")
        Integer minSelect,

        @NotNull(message = "NOT_NULL")
        @Min(value = 1, message = "INVALID_MIN")
        Integer maxSelect,

        @NotNull(message = "NOT_NULL")
        Long foodId,

        List<OptionItemRequest> items
) {
}
