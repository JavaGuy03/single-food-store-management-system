package com.fm.foodmanagementsystem.modules.order_service.resources.responses;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record CouponResponse(
        String id,
        String code,
        String discountType,
        Double discountValue,
        Double minOrderValue,
        Double maxDiscount,
        LocalDateTime expiresAt,
        Integer usageLimit,
        Integer usedCount,
        Boolean isActive
) {}