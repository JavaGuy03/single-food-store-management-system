package com.fm.foodmanagementsystem.core.services.imps;

import com.fm.foodmanagementsystem.core.services.interfaces.INotificationService;
import com.fm.foodmanagementsystem.modules.auth_service.models.entities.UserDevice;
import com.fm.foodmanagementsystem.modules.auth_service.models.repositories.UserDeviceRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class NotificationService implements INotificationService {

    UserDeviceRepository userDeviceRepository;

    @Override
    @Async
    public void sendNotificationToDevice(String targetToken, String title, String body, Map<String, String> data) {
        try {
            Message.Builder messageBuilder = Message.builder()
                    .setToken(targetToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build());

            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            String response = FirebaseMessaging.getInstance().send(messageBuilder.build());
            log.info("Successfully sent message to token: {}. Response: {}", targetToken, response);
        } catch (Exception e) {
            log.error("Error sending FCM notification to token {}: ", targetToken, e);
            if (e.getMessage() != null && (e.getMessage().contains("invalid token") || e.getMessage().contains("registration token"))) {
                userDeviceRepository.findByFcmToken(targetToken).ifPresent(userDeviceRepository::delete);
                log.info("Deleted invalid FCM token: {}", targetToken);
            }
        }
    }

    @Override
    @Async
    public void sendNotificationToTopic(String topic, String title, String body, Map<String, String> data) {
        try {
            Message.Builder messageBuilder = Message.builder()
                    .setTopic(topic)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build());

            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            String response = FirebaseMessaging.getInstance().send(messageBuilder.build());
            log.info("Successfully sent message to topic: {}. Response: {}", topic, response);
        } catch (Exception e) {
            log.error("Error sending FCM notification to topic {}: ", topic, e);
        }
    }

    @Override
    @Async
    public void sendNotificationToUser(String userId, String title, String body, Map<String, String> data) {
        List<UserDevice> devices = userDeviceRepository.findAllByUserId(userId);
        for (UserDevice device : devices) {
            sendNotificationToDevice(device.getFcmToken(), title, body, data);
        }
    }
}
