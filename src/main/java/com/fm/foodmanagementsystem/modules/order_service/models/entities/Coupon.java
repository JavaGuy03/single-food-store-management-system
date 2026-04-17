package com.fm.foodmanagementsystem.modules.order_service.models.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(unique = true, nullable = false)
    String code;

    @Column(name = "discount_type", nullable = false)
    String discountType;

    @Column(name = "discount_value", nullable = false)
    Double discountValue;

    @Column(name = "min_order_value")
    Double minOrderValue;

    @Column(name = "max_discount")
    Double maxDiscount;

    @Column(name = "expires_at", nullable = false)
    LocalDateTime expiresAt;

    @Column(name = "usage_limit")
    Integer usageLimit;

    @Column(name = "used_count", nullable = false)
    @Builder.Default
    Integer usedCount = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    Boolean isActive = true;
}