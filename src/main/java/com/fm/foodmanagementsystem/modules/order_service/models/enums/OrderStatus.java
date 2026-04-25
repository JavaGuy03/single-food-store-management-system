package com.fm.foodmanagementsystem.modules.order_service.models.enums;

public enum OrderStatus {
    PENDING,    // Chờ xử lý / Chờ thanh toán
    PAID,       // Đã thanh toán (ZaloPay)
    PREPARING,  // Đang chuẩn bị (Bếp đang làm)
    DELIVERING, // Đang giao hàng / Chờ lấy
    COMPLETED,  // Đã hoàn thành
    CANCELLED   // Đã hủy
}
