package com.fm.foodmanagementsystem.modules.notification_service.models.repositories;

import com.fm.foodmanagementsystem.modules.notification_service.models.entities.AppNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppNotificationRepository extends JpaRepository<AppNotification, Long> {
    Page<AppNotification> findAllByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    long countByUserIdAndIsReadFalse(String userId);
}
