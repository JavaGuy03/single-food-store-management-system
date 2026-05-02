package com.fm.foodmanagementsystem.modules.order_service.mappers;

import com.fm.foodmanagementsystem.modules.order_service.models.entities.Coupon;
import com.fm.foodmanagementsystem.modules.order_service.resources.responses.CouponResponse;
import org.springframework.stereotype.Component;

@Component
public class CouponMapper {

    public CouponResponse mapToResponse(Coupon coupon) {
        return CouponResponse.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .minOrderValue(coupon.getMinOrderValue())
                .maxDiscount(coupon.getMaxDiscount())
                .expiresAt(coupon.getExpiresAt())
                .usageLimit(coupon.getUsageLimit())
                .usedCount(coupon.getUsedCount())
                .reservedCount(coupon.getReservedCount() != null ? coupon.getReservedCount() : 0)
                .isActive(coupon.getIsActive())
                .build();
    }
}