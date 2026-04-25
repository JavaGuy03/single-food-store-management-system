package com.fm.foodmanagementsystem.modules.setting_service.resources.responses;

import lombok.Builder;

@Builder
public record StoreSettingResponse(
        Long id,
        String storeName,
        String hotline,
        Boolean isOpen,
        Double baseShippingFee,
        Double freeShipThreshold
) {
}
