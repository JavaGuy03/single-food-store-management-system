package com.fm.foodmanagementsystem.modules.notification_service.controllers;

import com.fm.foodmanagementsystem.core.response.ApiResponse;
import org.springframework.data.domain.Page;
import com.fm.foodmanagementsystem.modules.notification_service.resources.responses.AppNotificationResponse;
import com.fm.foodmanagementsystem.modules.notification_service.services.interfaces.IAppNotificationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user/notifications")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationController {

    IAppNotificationService notificationService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Page<AppNotificationResponse>> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.<Page<AppNotificationResponse>>builder()
                .result(notificationService.getMyNotifications(userId, page, size))
                .build();
    }

    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Long> countUnreadNotifications() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.<Long>builder()
                .result(notificationService.countUnreadNotifications(userId))
                .build();
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> markAsRead(@PathVariable Long id) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        notificationService.markAsRead(userId, id);
        return ApiResponse.<Void>builder().build();
    }

    @PatchMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> markAllAsRead() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        notificationService.markAllAsRead(userId);
        return ApiResponse.<Void>builder().build();
    }
}
