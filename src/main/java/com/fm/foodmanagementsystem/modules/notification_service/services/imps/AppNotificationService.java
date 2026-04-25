package com.fm.foodmanagementsystem.modules.notification_service.services.imps;

import com.fm.foodmanagementsystem.core.exception.SystemException;
import com.fm.foodmanagementsystem.core.exception.enums.SystemErrorCode;
import org.springframework.data.domain.PageImpl;
import com.fm.foodmanagementsystem.modules.notification_service.models.entities.AppNotification;
import com.fm.foodmanagementsystem.modules.notification_service.models.repositories.AppNotificationRepository;
import com.fm.foodmanagementsystem.modules.notification_service.resources.responses.AppNotificationResponse;
import com.fm.foodmanagementsystem.modules.notification_service.services.interfaces.IAppNotificationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AppNotificationService implements IAppNotificationService {

    AppNotificationRepository notificationRepository;

    @Override
    public Page<AppNotificationResponse> getMyNotifications(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AppNotification> notifications = notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId, pageable);

        List<AppNotificationResponse> content = notifications.getContent().stream()
                .map(n -> AppNotificationResponse.builder()
                        .id(n.getId())
                        .title(n.getTitle())
                        .body(n.getBody())
                        .orderId(n.getOrderId())
                        .isRead(n.getIsRead())
                        .createdAt(n.getCreatedAt())
                        .build())
                .toList();

        return new PageImpl<>(content, pageable, notifications.getTotalElements());
    }

    @Override
    public long countUnreadNotifications(String userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    @Transactional
    public void markAsRead(String userId, Long notificationId) {
        AppNotification notification = notificationRepository.findById(notificationId)
                .filter(n -> n.getUser().getId().equals(userId))
                .orElseThrow(() -> new SystemException(SystemErrorCode.DATA_NOT_FOUND));

        if (!notification.getIsRead()) {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        }
    }

    @Override
    @Transactional
    public void markAllAsRead(String userId) {
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        Page<AppNotification> notifications = notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId, pageable);
        
        for (AppNotification notification : notifications.getContent()) {
            if (!notification.getIsRead()) {
                notification.setIsRead(true);
                notificationRepository.save(notification);
            }
        }
    }
}
