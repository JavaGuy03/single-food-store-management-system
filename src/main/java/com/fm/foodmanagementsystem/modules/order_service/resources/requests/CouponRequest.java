package com.fm.foodmanagementsystem.modules.order_service.resources.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record CouponRequest(
        @NotBlank(message = "NOT_BLANK")
        String code,

        @NotBlank(message = "NOT_BLANK")
        String discountType,

        @NotNull(message = "NOT_NULL")
        @Min(value = 0, message = "INVALID_MIN")
        Double discountValue,

        Double minOrderValue,

        Double maxDiscount,

        @NotNull(message = "NOT_NULL")
        LocalDateTime expiresAt,

        Integer usageLimit
) {}