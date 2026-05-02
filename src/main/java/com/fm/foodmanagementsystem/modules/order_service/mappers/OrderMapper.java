package com.fm.foodmanagementsystem.modules.order_service.mappers;

import com.fm.foodmanagementsystem.modules.auth_service.models.entities.User;
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

    /**
     * Map an Order to response. Pass the resolved User if already available (avoids extra DB hit).
     * Pass null if user lookup is not needed or not available.
     */
    public OrderResponse mapToResponse(Order order, User user) {
        List<OrderItemResponse> itemResponses = order.getOrderItems() != null
                ? order.getOrderItems().stream().map(this::mapToItemResponse).toList()
                : Collections.emptyList();

        String customerName = user != null ? (user.getLastName() + " " + user.getFirstName()) : "Không rõ";
        String customerPhone = user != null ? user.getPhone() : "Không rõ";

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .customerName(customerName)
                .customerPhone(customerPhone)
                .orderDate(order.getOrderDate())
                .totalAmount(order.getTotalAmount())
                .shippingFee(order.getShippingFee() != null ? order.getShippingFee() : 0.0)
                .status(order.getStatus() != null ? order.getStatus().name() : null)
                .couponCode(order.getCouponCode())
                .discountAmount(order.getDiscountAmount())
                .deliveryAddress(order.getDeliveryAddress())
                .note(order.getNote())
                .itemsSummary(order.getItemsSummary())
                .orderItems(itemResponses)
                .build();
    }

    /** Convenience overload when user is not available (admin list, pagination). */
    public OrderResponse mapToResponse(Order order) {
        return mapToResponse(order, null);
    }
}