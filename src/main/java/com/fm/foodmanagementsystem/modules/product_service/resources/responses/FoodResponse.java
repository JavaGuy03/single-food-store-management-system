package com.fm.foodmanagementsystem.modules.product_service.resources.responses;

import lombok.Builder;

import java.util.List;

@Builder
public record FoodResponse(
        Long id,
        String name,
        String description,
        Double price,
        Boolean isAvailable,
        String imageUrl,
        Long categoryId,
        String categoryName,
        List<OptionGroupResponse> optionGroups
) {}