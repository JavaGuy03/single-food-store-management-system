package com.fm.foodmanagementsystem.modules.auth_service.services.imps;

import com.fm.foodmanagementsystem.core.exception.SystemException;
import com.fm.foodmanagementsystem.core.exception.enums.SystemErrorCode;
import com.fm.foodmanagementsystem.modules.auth_service.models.entities.User;
import com.fm.foodmanagementsystem.modules.auth_service.models.entities.UserDevice;
import com.fm.foodmanagementsystem.modules.auth_service.models.repositories.UserDeviceRepository;
import com.fm.foodmanagementsystem.modules.auth_service.models.repositories.UserRepository;
import com.fm.foodmanagementsystem.modules.auth_service.resources.requests.FcmTokenRequest;
import com.fm.foodmanagementsystem.modules.auth_service.services.interfaces.IUserDeviceService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserDeviceService implements IUserDeviceService {

    UserDeviceRepository deviceRepository;
    UserRepository userRepository;

    @Override
    @Transactional
    public void registerDevice(String userId, FcmTokenRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new SystemException(SystemErrorCode.USER_NOT_EXISTED));

        deviceRepository.findByFcmToken(request.fcmToken()).ifPresentOrElse(
                existingDevice -> {
                    // Cập nhật last active và user nếu user chuyển tài khoản trên cùng 1 máy
                    existingDevice.setUser(user);
                    existingDevice.setLastActive(LocalDateTime.now());
                    deviceRepository.save(existingDevice);
                },
                () -> {
                    // Thiết bị mới
                    UserDevice newDevice = UserDevice.builder()
                            .user(user)
                            .fcmToken(request.fcmToken())
                            .deviceType(request.deviceType())
                            .build();
                    deviceRepository.save(newDevice);
                }
        );
    }

    @Override
    @Transactional
    public void unregisterDevice(String userId, String fcmToken) {
        UserDevice device = deviceRepository.findByFcmToken(fcmToken).orElse(null);
        if (device == null) {
            return;
        }
        if (!device.getUser().getId().equals(userId)) {
            throw new SystemException(SystemErrorCode.UNAUTHORIZED_ACTION);
        }
        deviceRepository.delete(device);
    }
}
