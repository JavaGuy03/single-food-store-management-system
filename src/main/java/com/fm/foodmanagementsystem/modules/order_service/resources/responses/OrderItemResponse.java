package com.fm.foodmanagementsystem.modules.order_service.resources.responses;

import lombok.Builder;

import java.util.List;

@Builder
public record OrderItemResponse(
        Long id,
        Long foodId,
        String foodName,
        Integer quantity,
        Double unitPrice,
        Double totalPrice,
        List<String> selectedOptions
) {}