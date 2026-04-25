package com.fm.foodmanagementsystem.modules.interaction_service.resources.responses;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ReviewResponse(
        Long id,
        String userId,
        String userFullName,
        String orderId,
        Integer rating,
        String comment,
        LocalDateTime createdAt
) {
}
