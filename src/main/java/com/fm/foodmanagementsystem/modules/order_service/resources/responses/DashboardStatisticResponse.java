package com.fm.foodmanagementsystem.modules.order_service.resources.responses;

import lombok.Builder;

@Builder
public record DashboardStatisticResponse(
        long todayOrders,
        double todayRevenue,
        long totalFoods
) {
}