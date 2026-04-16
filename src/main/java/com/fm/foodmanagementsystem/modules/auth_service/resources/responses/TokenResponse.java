package com.fm.foodmanagementsystem.modules.auth_service.resources.responses;

import lombok.Builder;

@Builder
public record TokenResponse(
        String accessToken,
        String refreshToken,
        boolean authenticated
) {
}