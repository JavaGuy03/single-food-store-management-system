package com.fm.foodmanagementsystem.modules.order_service.mappers;

import com.fm.foodmanagementsystem.modules.auth_service.models.entities.User;
import com.fm.foodmanagementsystem.modules.auth_service.models.repositories.UserRepository;
import com.fm.foodmanagementsystem.modules.order_service.models.entities.Order;
import com.fm.foodmanagementsystem.modules.order_service.models.entities.OrderItem;
import com.fm.foodmanagementsystem.modules.order_service.resources.responses.OrderItemResponse;
import com.fm.foodmanagementsystem.modules.order_service.resources.responses.OrderResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class OrderMapper {

    @Autowired
    private UserRepository userRepository;

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

        User user = null;
        if (order.getUserId() != null) {
            user = userRepository.findById(order.getUserId()).orElse(null);
        }

        String customerName = "Không rõ";
        String customerPhone = "Không rõ";

        if (user != null) {
            customerName = user.getLastName() + " " + user.getFirstName();
            customerPhone = user.getPhone();
        }

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .customerName(customerName)
                .customerPhone(customerPhone)
                .orderDate(order.getOrderDate())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus() != null ? order.getStatus().name() : null)
                .couponCode(order.getCouponCode())
                .discountAmount(order.getDiscountAmount())
                .deliveryAddress(order.getDeliveryAddress())
                .note(order.getNote())
                .itemsSummary(order.getItemsSummary())
                .orderItems(itemResponses)
                .build();
    }
}