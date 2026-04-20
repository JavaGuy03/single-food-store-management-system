package com.fm.foodmanagementsystem.modules.order_service.resources.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record OrderRequest(
        @NotBlank(message = "NOT_BLANK")
        String deliveryAddress,

        String note,

        String couponCode,

        @NotEmpty(message = "NOT_EMPTY")
        List<OrderItemRequest> items
) {}