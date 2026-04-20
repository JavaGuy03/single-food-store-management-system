package com.fm.foodmanagementsystem.modules.product_service.resources.responses;

import lombok.Builder;

import java.util.List;

@Builder
public record OptionGroupResponse(
        Long id,
        String name,
        Integer minSelect,
        Integer maxSelect,
        Long foodId,
        List<OptionItemResponse> items
) {
}
