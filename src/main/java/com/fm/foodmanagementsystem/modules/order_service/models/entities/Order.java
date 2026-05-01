package com.fm.foodmanagementsystem.modules.order_service.models.entities;

import com.fm.foodmanagementsystem.modules.order_service.models.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(name = "user_id", nullable = false)
    String userId;

    @Column(name = "order_date", nullable = false)
    LocalDateTime orderDate;

    @Column(name = "total_amount", nullable = false)
    Double totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    OrderStatus status;

    @ElementCollection
    @CollectionTable(name = "order_items_summary", joinColumns = @JoinColumn(name = "order_id"))
    @Column(name = "item_summary")
    List<String> itemsSummary;

    @Column(name = "delivery_address", nullable = false)
    String deliveryAddress;

    String note;

    @Column(name = "coupon_code")
    String couponCode;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    List<OrderItem> orderItems;
}