package com.fm.foodmanagementsystem.modules.setting_service.models.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "banners")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Banner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String title;

    @Column(name = "image_name", nullable = false)
    String imageName;

    @Column(name = "link_url")
    String linkUrl; // (Optional) Link tới chương trình KM hoặc món ăn

    @Column(name = "is_active")
    @Builder.Default
    Boolean isActive = true;

    @Column(name = "display_order")
    @Builder.Default
    Integer displayOrder = 0;
}
