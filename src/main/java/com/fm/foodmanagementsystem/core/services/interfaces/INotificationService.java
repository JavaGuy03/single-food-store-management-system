package com.fm.foodmanagementsystem.core.services.interfaces;

import java.util.Map;

public interface INotificationService {
    void sendNotificationToDevice(String targetToken, String title, String body, Map<String, String> data);
    void sendNotificationToTopic(String topic, String title, String body, Map<String, String> data);
    void sendNotificationToUser(String userId, String title, String body, Map<String, String> data);
}
