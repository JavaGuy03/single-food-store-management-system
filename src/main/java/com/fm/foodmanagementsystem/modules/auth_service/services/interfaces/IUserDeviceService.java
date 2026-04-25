package com.fm.foodmanagementsystem.modules.auth_service.services.interfaces;

import com.fm.foodmanagementsystem.modules.auth_service.resources.requests.FcmTokenRequest;

public interface IUserDeviceService {
    void registerDevice(String userId, FcmTokenRequest request);
    void unregisterDevice(String fcmToken);
}
