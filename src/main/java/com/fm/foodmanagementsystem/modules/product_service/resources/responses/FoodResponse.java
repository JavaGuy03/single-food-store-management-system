package com.fm.foodmanagementsystem.modules.product_service.resources.responses;

import lombok.Builder;

import java.util.List;
import com.fm.foodmanagementsystem.modules.interaction_service.resources.responses.ReviewResponse;

@Builder(toBuilder = true)
public record FoodResponse(
        Long id,
        String name,
        String description,
        Double price,
        Boolean isAvailable,
        String imageUrl,
        Long categoryId,
        String categoryName,
        List<OptionGroupResponse> optionGroups,
        Double rating,
        Long totalReviews,
        List<ReviewResponse> reviews
) {}