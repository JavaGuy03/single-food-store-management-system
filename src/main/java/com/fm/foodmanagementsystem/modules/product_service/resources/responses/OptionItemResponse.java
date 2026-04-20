package com.fm.foodmanagementsystem.modules.product_service.resources.responses;

import lombok.Builder;

@Builder
public record OptionItemResponse(
        Long id,
        String name,
        Double priceAdjustment
) {
}
