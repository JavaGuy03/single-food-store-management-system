package com.fm.foodmanagementsystem.modules.product_service.models.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;

@Entity
@Table(name = "option_groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OptionGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String name; // Ví dụ: "Toppings", "Size"

    @Column(name = "min_select")
    @Builder.Default
    Integer minSelect = 0; // Ít nhất phải chọn bao nhiêu (0 là không bắt buộc)

    @Column(name = "max_select")
    @Builder.Default
    Integer maxSelect = 1; // Được chọn tối đa bao nhiêu

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id")
    Food food;

    @OneToMany(mappedBy = "optionGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    List<OptionItem> items;
}