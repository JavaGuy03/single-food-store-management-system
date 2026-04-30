package com.fm.foodmanagementsystem.modules.order_service.resources.responses;

import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record OrderResponse(
        String id,
        String userId,
        String customerName,
        String customerPhone,
        LocalDateTime orderDate,
        Double totalAmount,
        String status,
        String deliveryAddress,
        String note,
        List<String> itemsSummary,
        List<OrderItemResponse> orderItems
) {}