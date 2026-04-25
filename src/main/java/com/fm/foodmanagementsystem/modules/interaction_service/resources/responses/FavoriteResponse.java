package com.fm.foodmanagementsystem.modules.interaction_service.resources.responses;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record FavoriteResponse(
        Long id,
        Long foodId,
        String foodName,
        Double price,
        String imageUrl,
        LocalDateTime createdAt
) {
}
