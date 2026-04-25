package com.fm.foodmanagementsystem.modules.order_service.services.interfaces;

import com.fm.foodmanagementsystem.modules.order_service.resources.requests.OrderRequest;
import com.fm.foodmanagementsystem.modules.order_service.resources.responses.OrderResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IOrderService {
    OrderResponse createOrder(String userId, OrderRequest request);
    List<OrderResponse> getMyOrders(String userId);
    OrderResponse getOrderById(String id);
    void updateOrderStatus(String orderId, String status);
    Page<OrderResponse> getAllOrders(String status, int page, int size);
}