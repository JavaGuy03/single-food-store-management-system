package com.fm.foodmanagementsystem.modules.product_service.models.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "option_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OptionItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String name; // Ví dụ: "Trân châu đen", "Size L"

    @Column(name = "price_adjustment")
    Double priceAdjustment; // Giá cộng thêm (ví dụ: 5000.0)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    OptionGroup optionGroup;
}