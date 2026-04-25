package com.fm.foodmanagementsystem.modules.notification_service.services.interfaces;

import org.springframework.data.domain.Page;
import com.fm.foodmanagementsystem.modules.notification_service.resources.responses.AppNotificationResponse;

public interface IAppNotificationService {
    Page<AppNotificationResponse> getMyNotifications(String userId, int page, int size);
    long countUnreadNotifications(String userId);
    void markAsRead(String userId, Long notificationId);
    void markAllAsRead(String userId);
}
