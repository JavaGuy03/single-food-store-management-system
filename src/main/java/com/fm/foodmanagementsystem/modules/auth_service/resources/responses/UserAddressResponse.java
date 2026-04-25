package com.fm.foodmanagementsystem.modules.auth_service.resources.responses;

import lombok.Builder;

@Builder
public record UserAddressResponse(
        Long id,
        String title,
        String address,
        Double latitude,
        Double longitude,
        Boolean isDefault
) {
}
