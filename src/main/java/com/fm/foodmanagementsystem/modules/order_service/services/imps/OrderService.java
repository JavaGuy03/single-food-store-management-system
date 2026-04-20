package com.fm.foodmanagementsystem.modules.order_service.services.imps;

import com.fm.foodmanagementsystem.core.exception.SystemException;
import com.fm.foodmanagementsystem.core.exception.enums.SystemErrorCode;
import com.fm.foodmanagementsystem.modules.order_service.mappers.OrderMapper;
import com.fm.foodmanagementsystem.modules.order_service.models.entities.Coupon;
import com.fm.foodmanagementsystem.modules.order_service.models.entities.Order;
import com.fm.foodmanagementsystem.modules.order_service.models.entities.OrderItem;
import com.fm.foodmanagementsystem.modules.order_service.models.repositories.CouponRepository;
import com.fm.foodmanagementsystem.modules.order_service.models.repositories.OrderRepository;
import com.fm.foodmanagementsystem.modules.order_service.resources.requests.OrderItemRequest;
import com.fm.foodmanagementsystem.modules.order_service.resources.requests.OrderRequest;
import com.fm.foodmanagementsystem.modules.order_service.resources.responses.OrderResponse;
import com.fm.foodmanagementsystem.modules.order_service.services.interfaces.IOrderService;
import com.fm.foodmanagementsystem.modules.product_service.models.entities.Food;
import com.fm.foodmanagementsystem.modules.product_service.models.entities.OptionItem;
import com.fm.foodmanagementsystem.modules.product_service.models.repositories.FoodRepository;
import com.fm.foodmanagementsystem.modules.product_service.models.repositories.OptionItemRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderService implements IOrderService {

    OrderRepository orderRepository;
    FoodRepository foodRepository;
    OptionItemRepository optionItemRepository;
    CouponRepository couponRepository;
    OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderResponse createOrder(String userId, OrderRequest request) {
        Order order = new Order();
        order.setUserId(userId);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");
        order.setDeliveryAddress(request.deliveryAddress());
        order.setNote(request.note());

        double totalAmount = 0.0;
        List<OrderItem> orderItems = new ArrayList<>();
        List<String> itemsSummary = new ArrayList<>();

        for (OrderItemRequest itemReq : request.items()) {
            Food food = foodRepository.findById(itemReq.foodId())
                    .orElseThrow(() -> new SystemException(SystemErrorCode.DATA_NOT_FOUND));

            double unitPrice = food.getPrice();
            List<String> optionNames = new ArrayList<>();

            if (itemReq.selectedOptionIds() != null && !itemReq.selectedOptionIds().isEmpty()) {
                List<OptionItem> options = optionItemRepository.findAllById(itemReq.selectedOptionIds());
                for (OptionItem opt : options) {
                    unitPrice += opt.getPriceAdjustment();
                    optionNames.add(opt.getName());
                }
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setFood(food);
            orderItem.setQuantity(itemReq.quantity());
            orderItem.setUnitPrice(unitPrice);
            orderItem.setSelectedOptions(optionNames);

            orderItems.add(orderItem);
            totalAmount += (unitPrice * itemReq.quantity());

            String summary = itemReq.quantity() + "x " + food.getName();
            if (!optionNames.isEmpty()) summary += " (" + String.join(", ", optionNames) + ")";
            itemsSummary.add(summary);
        }

        if (request.couponCode() != null && !request.couponCode().trim().isEmpty()) {
            Coupon coupon = couponRepository.findByCode(request.couponCode().toUpperCase())
                    .orElseThrow(() -> new SystemException(SystemErrorCode.DATA_NOT_FOUND));

            if (!coupon.getIsActive() || coupon.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new SystemException(SystemErrorCode.DATA_NOT_FOUND);
            }
            if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) {
                throw new SystemException(SystemErrorCode.DATA_NOT_FOUND);
            }
            if (coupon.getMinOrderValue() != null && totalAmount < coupon.getMinOrderValue()) {
                throw new SystemException(SystemErrorCode.DATA_NOT_FOUND);
            }

            double discountAmount = 0.0;
            if ("percent".equalsIgnoreCase(coupon.getDiscountType())) {
                discountAmount = totalAmount * (coupon.getDiscountValue() / 100.0);
                if (coupon.getMaxDiscount() != null && discountAmount > coupon.getMaxDiscount()) {
                    discountAmount = coupon.getMaxDiscount();
                }
            } else if ("fixed".equalsIgnoreCase(coupon.getDiscountType())) {
                discountAmount = coupon.getDiscountValue();
            }

            totalAmount = Math.max(0, totalAmount - discountAmount);

            coupon.setUsedCount(coupon.getUsedCount() + 1);
            couponRepository.save(coupon);
        }

        order.setOrderItems(orderItems);
        order.setItemsSummary(itemsSummary);
        order.setTotalAmount(totalAmount);

        return orderMapper.mapToResponse(orderRepository.save(order));
    }

    @Override
    public List<OrderResponse> getMyOrders(String userId) {
        return orderRepository.findAllByUserId(userId).stream()
                .map(orderMapper::mapToResponse)
                .toList();
    }

    @Override
    public OrderResponse getOrderById(String id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new SystemException(SystemErrorCode.DATA_NOT_FOUND));
        return orderMapper.mapToResponse(order);
    }

    @Override
    @Transactional
    public void updateOrderStatus(String orderId, String status) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new SystemException(SystemErrorCode.DATA_NOT_FOUND));
        order.setStatus(status);
        orderRepository.save(order);
    }
}