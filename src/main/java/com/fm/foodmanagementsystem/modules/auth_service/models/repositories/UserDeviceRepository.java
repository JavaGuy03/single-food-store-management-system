package com.fm.foodmanagementsystem.modules.auth_service.models.repositories;

import com.fm.foodmanagementsystem.modules.auth_service.models.entities.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {
    List<UserDevice> findAllByUserId(String userId);
    Optional<UserDevice> findByFcmToken(String fcmToken);
    void deleteByFcmToken(String fcmToken);
}
