package com.fm.foodmanagementsystem.modules.order_service.services.interfaces;

import com.fm.foodmanagementsystem.modules.order_service.resources.responses.DashboardStatisticResponse;

import java.time.LocalDate;

public interface IStatisticService {
    DashboardStatisticResponse getDashboardOverview();
    byte[] exportDailyRevenueReport(LocalDate date);
}
