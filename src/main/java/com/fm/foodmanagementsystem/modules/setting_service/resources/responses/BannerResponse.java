package com.fm.foodmanagementsystem.modules.setting_service.resources.responses;

import lombok.Builder;

@Builder
public record BannerResponse(
        Long id,
        String title,
        String imageUrl,
        String linkUrl,
        Boolean isActive,
        Integer displayOrder
) {
}
