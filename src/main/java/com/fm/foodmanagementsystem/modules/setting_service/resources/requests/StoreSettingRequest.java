package com.fm.foodmanagementsystem.modules.setting_service.resources.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StoreSettingRequest(
        @NotBlank(message = "NOT_BLANK")
        String storeName,

        @NotBlank(message = "NOT_BLANK")
        String hotline,

        @NotNull(message = "NOT_NULL")
        Boolean isOpen,

        @NotNull(message = "NOT_NULL")
        @Min(value = 0, message = "INVALID_MIN")
        Double baseShippingFee,

        @NotNull(message = "NOT_NULL")
        @Min(value = 0, message = "INVALID_MIN")
        Double freeShipThreshold
) {
}
