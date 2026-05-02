package com.fm.foodmanagementsystem.modules.interaction_service.resources.responses;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ReviewResponse(
        Long id,
        String userId,
        String userFullName,
        String orderId,
        Long foodId,       // C-6: which food was rated
        String foodName,   // C-6: food name for display
        Integer rating,
        String comment,
        LocalDateTime createdAt
) {
}
