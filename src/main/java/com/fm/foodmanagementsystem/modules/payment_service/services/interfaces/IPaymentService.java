package com.fm.foodmanagementsystem.modules.payment_service.services.interfaces;

import java.util.Map;

public interface IPaymentService {
    Map<String, Object> createZaloPayOrder(String orderId, double amount);
    Map<String, Object> queryZaloPayOrder(String appTransId);
}