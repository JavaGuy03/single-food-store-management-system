package com.fm.foodmanagementsystem.modules.auth_service.resources.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserAddressRequest(
        @NotBlank(message = "NOT_BLANK")
        String title,

        @NotBlank(message = "NOT_BLANK")
        String address,

        @NotNull(message = "NOT_NULL")
        Double latitude,

        @NotNull(message = "NOT_NULL")
        Double longitude,

        Boolean isDefault
) {
}
