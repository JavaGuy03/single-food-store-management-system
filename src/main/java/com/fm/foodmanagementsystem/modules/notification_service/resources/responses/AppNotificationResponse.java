package com.fm.foodmanagementsystem.modules.notification_service.resources.responses;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AppNotificationResponse(
        Long id,
        String title,
        String body,
        String orderId,
        Boolean isRead,
        LocalDateTime createdAt
) {
}
