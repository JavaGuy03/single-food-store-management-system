package com.fm.foodmanagementsystem.modules.order_service.mappers;

import com.fm.foodmanagementsystem.modules.order_service.models.entities.Order;
import com.fm.foodmanagementsystem.modules.order_service.models.entities.OrderItem;
import com.fm.foodmanagementsystem.modules.order_service.resources.responses.OrderItemResponse;
import com.fm.foodmanagementsystem.modules.order_service.resources.responses.OrderResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class OrderMapper {

    public OrderItemResponse mapToItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .foodId(item.getFood() != null ? item.getFood().getId() : null)
                .foodName(item.getFood() != null ? item.getFood().getName() : null)
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getQuantity() * item.getUnitPrice())
                .selectedOptions(item.getSelectedOptions())
                .build();
    }

    public OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getOrderItems() != null
                ? order.getOrderItems().stream().map(this::mapToItemResponse).toList()
                : Collections.emptyList();

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .orderDate(order.getOrderDate())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus() != null ? order.getStatus().name() : null)
                .deliveryAddress(order.getDeliveryAddress())
                .note(order.getNote())
                .itemsSummary(order.getItemsSummary())
                .orderItems(itemResponses)
                .build();
    }
}