package com.fm.foodmanagementsystem.modules.notification_service.controllers;

import com.fm.foodmanagementsystem.core.response.ApiResponse;
import com.fm.foodmanagementsystem.core.services.interfaces.INotificationService;
import com.fm.foodmanagementsystem.modules.auth_service.models.repositories.UserRepository;
import com.fm.foodmanagementsystem.modules.notification_service.models.entities.AppNotification;
import com.fm.foodmanagementsystem.modules.notification_service.models.repositories.AppNotificationRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/notifications")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminNotificationController {

    INotificationService notificationService;
    AppNotificationRepository notificationRepository;
    UserRepository userRepository;

    @PostMapping("/broadcast")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> broadcastToCustomers(@RequestBody BroadcastRequest request) {
        // Lưu thông báo vào DB cho từng khách hàng active
        var customers = userRepository.findAllActiveByRoleName("USER");
        List<AppNotification> records = customers.stream()
                .map(user -> AppNotification.builder()
                        .user(user)
                        .title(request.title())
                        .body(request.body())
                        .build())
                .toList();
        notificationRepository.saveAll(records);

        // Gửi FCM đến tất cả thiết bị đăng ký topic
        notificationService.sendNotificationToTopic(
                "customer_promotions",
                request.title(),
                request.body(),
                Map.of()
        );

        return ApiResponse.<String>builder()
                .message("Đã gửi thông báo đến " + records.size() + " khách hàng")
                .build();
    }

    record BroadcastRequest(String title, String body) {}
}
