package com.fm.foodmanagementsystem.modules.auth_service.models.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_devices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDevice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Column(name = "fcm_token", nullable = false, unique = true)
    String fcmToken; // Firebase Cloud Messaging Token

    @Column(name = "device_type")
    String deviceType; // "ANDROID", "IOS", "WEB"

    @Column(name = "last_active")
    LocalDateTime lastActive;

    @PrePersist
    protected void onCreate() {
        lastActive = LocalDateTime.now();
    }
}
