package com.fm.foodmanagementsystem.modules.order_service.models.entities;

import com.fm.foodmanagementsystem.modules.product_service.models.entities.Food;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id", nullable = false)
    Food food;

    @Column(nullable = false)
    Integer quantity;

    @Column(name = "unit_price", nullable = false)
    Double unitPrice;

    @ElementCollection
    @CollectionTable(name = "order_item_selected_options", joinColumns = @JoinColumn(name = "order_item_id"))
    @Column(name = "option_name")
    List<String> selectedOptions;
}