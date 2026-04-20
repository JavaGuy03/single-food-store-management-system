package com.fm.foodmanagementsystem.modules.product_service.resources.responses;

import lombok.Builder;

@Builder
public record CategoryResponse(
        Long id,
        String name,
        String description,
        String imageUrl
) {}