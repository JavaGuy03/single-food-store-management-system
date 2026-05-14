package com.fm.foodmanagementsystem.modules.setting_service.models.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "store_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StoreSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "store_name")
    String storeName;

    @Column(name = "hotline")
    String hotline;

    @Column(name = "is_open")
    @Builder.Default
    Boolean isOpen = true;

    @Column(name = "base_shipping_fee")
    @Builder.Default
    Double baseShippingFee = 15000.0;

    @Column(name = "free_ship_threshold")
    @Builder.Default
    Double freeShipThreshold = 300000.0;

    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
    }

    @PrePersist
    protected void onCreate() {
        updatedAt = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
    }
}
