package com.fm.foodmanagementsystem.modules.payment_service.models.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "order_id", nullable = false)
    String orderId;

    @Column(name = "user_id", nullable = false)
    String userId; // Để sau này query lịch sử theo user cho lẹ

    @Column(nullable = false)
    Double amount;

    @Column(name = "payment_method")
    String paymentMethod; // VD: "ZALOPAY", "CASH"

    @Column(name = "app_trans_id")
    String appTransId; // Mã giao dịch do hệ thống mình sinh ra gửi cho ZaloPay

    @Column(name = "zp_trans_id")
    String zpTransId; // Mã giao dịch do ZaloPay sinh ra (Có khi thanh toán thành công)

    /** PENDING, SUCCESS, FAILED, REFUNDED, RECONCILE_REQUIRED — cổ báo đã charge nhưng đơn chưa PAID được. */
    @Column(nullable = false)
    String status;

    @Column(name = "created_at")
    LocalDateTime createdAt;

    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}