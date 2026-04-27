package com.fm.foodmanagementsystem.modules.order_service.services.imps;

import com.fm.foodmanagementsystem.core.exception.SystemException;
import com.fm.foodmanagementsystem.core.exception.enums.SystemErrorCode;
import com.fm.foodmanagementsystem.core.services.interfaces.INotificationService;
import com.fm.foodmanagementsystem.modules.notification_service.models.entities.AppNotification;
import com.fm.foodmanagementsystem.modules.notification_service.models.repositories.AppNotificationRepository;
import com.fm.foodmanagementsystem.modules.auth_service.models.entities.User;
import com.fm.foodmanagementsystem.modules.auth_service.models.repositories.UserRepository;
import com.fm.foodmanagementsystem.modules.order_service.mappers.OrderMapper;
import com.fm.foodmanagementsystem.modules.order_service.models.entities.Coupon;
import com.fm.foodmanagementsystem.modules.order_service.models.entities.Order;
import com.fm.foodmanagementsystem.modules.order_service.models.entities.OrderItem;
import com.fm.foodmanagementsystem.modules.order_service.models.enums.OrderStatus;
import com.fm.foodmanagementsystem.modules.order_service.models.repositories.CouponRepository;
import com.fm.foodmanagementsystem.modules.order_service.models.repositories.OrderRepository;
import com.fm.foodmanagementsystem.modules.order_service.resources.requests.OrderItemRequest;
import com.fm.foodmanagementsystem.modules.order_service.resources.requests.OrderRequest;
import com.fm.foodmanagementsystem.modules.order_service.resources.responses.OrderResponse;
import com.fm.foodmanagementsystem.modules.order_service.services.interfaces.IOrderService;
import com.fm.foodmanagementsystem.modules.product_service.models.entities.Food;
import com.fm.foodmanagementsystem.modules.product_service.models.entities.OptionGroup;
import com.fm.foodmanagementsystem.modules.product_service.models.entities.OptionItem;
import com.fm.foodmanagementsystem.modules.product_service.models.repositories.FoodRepository;
import com.fm.foodmanagementsystem.modules.product_service.models.repositories.OptionGroupRepository;
import com.fm.foodmanagementsystem.modules.product_service.models.repositories.OptionItemRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderService implements IOrderService {

    OrderRepository orderRepository;
    FoodRepository foodRepository;
    OptionItemRepository optionItemRepository;
    OptionGroupRepository optionGroupRepository;
    CouponRepository couponRepository;
    UserRepository userRepository;
    OrderMapper orderMapper;
    INotificationService notificationService;
    AppNotificationRepository appNotificationRepository;

    // M3: Định nghĩa luồng chuyển trạng thái hợp lệ
    private static final Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS = Map.of(
            OrderStatus.PENDING, Set.of(OrderStatus.PAID, OrderStatus.CANCELLED),
            OrderStatus.PAID, Set.of(OrderStatus.PREPARING, OrderStatus.CANCELLED),
            OrderStatus.PREPARING, Set.of(OrderStatus.DELIVERING, OrderStatus.CANCELLED),
            OrderStatus.DELIVERING, Set.of(OrderStatus.COMPLETED),
            OrderStatus.COMPLETED, Set.of(),
            OrderStatus.CANCELLED, Set.of()
    );

    @Override
    @Transactional
    public OrderResponse createOrder(String userId, OrderRequest request) {
        boolean isActiveUser = userRepository.findById(userId)
                .map(u -> u.getIsActive())
                .orElseThrow(() -> new SystemException(SystemErrorCode.USER_NOT_EXISTED));

        if (!isActiveUser) {
            throw new SystemException(SystemErrorCode.USER_DISABLED);
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setDeliveryAddress(request.deliveryAddress());
        order.setNote(request.note());

        double totalAmount = 0.0;
        List<OrderItem> orderItems = new ArrayList<>();
        List<String> itemsSummary = new ArrayList<>();

        for (OrderItemRequest itemReq : request.items()) {
            Food food = foodRepository.findById(itemReq.foodId())
                    .orElseThrow(() -> new SystemException(SystemErrorCode.DATA_NOT_FOUND));

            // M5: Dùng error code riêng cho món ăn ngừng bán
            if (!food.getIsAvailable()) {
                throw new SystemException(SystemErrorCode.FOOD_UNAVAILABLE);
            }

            double unitPrice = food.getPrice();
            List<String> optionNames = new ArrayList<>();

            List<OptionItem> selectedOptions = new ArrayList<>();
            if (itemReq.selectedOptionIds() != null && !itemReq.selectedOptionIds().isEmpty()) {
                selectedOptions = optionItemRepository.findAllById(itemReq.selectedOptionIds());
            }

            Map<Long, Long> submittedCountByGroupId = selectedOptions.stream()
                    .collect(Collectors.groupingBy(opt -> opt.getOptionGroup().getId(), Collectors.counting()));

            List<OptionGroup> allFoodGroups = optionGroupRepository.findAllByFoodId(food.getId());

            for (OptionGroup group : allFoodGroups) {
                long selectedCount = submittedCountByGroupId.getOrDefault(group.getId(), 0L);
                if (selectedCount < group.getMinSelect() || selectedCount > group.getMaxSelect()) {
                    throw new SystemException(SystemErrorCode.INVALID_PARAMETER);
                }
            }

            for (OptionItem opt : selectedOptions) {
                if (!opt.getOptionGroup().getFood().getId().equals(food.getId())) {
                    throw new SystemException(SystemErrorCode.INVALID_PARAMETER);
                }
                unitPrice += opt.getPriceAdjustment();
                optionNames.add(opt.getName());
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

        // ÁP DỤNG KHOÁ PESSIMISTIC CHO COUPON
        if (request.couponCode() != null && !request.couponCode().trim().isEmpty()) {
            Coupon coupon = couponRepository.findByCodeWithLock(request.couponCode().toUpperCase())
                    .orElseThrow(() -> new SystemException(SystemErrorCode.DATA_NOT_FOUND));

            // M4: Dùng error code riêng cho từng trường hợp coupon lỗi
            if (!coupon.getIsActive() || coupon.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new SystemException(SystemErrorCode.COUPON_EXPIRED);
            }
            if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) {
                throw new SystemException(SystemErrorCode.COUPON_USAGE_LIMIT);
            }
            if (coupon.getMinOrderValue() != null && totalAmount < coupon.getMinOrderValue()) {
                throw new SystemException(SystemErrorCode.COUPON_MIN_ORDER);
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

    // M3: Validate trạng thái chuyển đổi hợp lệ
    @Override
    @Transactional
    public void updateOrderStatus(String orderId, String status) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new SystemException(SystemErrorCode.DATA_NOT_FOUND));

        try {
            OrderStatus newStatus = OrderStatus.valueOf(status.toUpperCase());

            // Kiểm tra luồng chuyển trạng thái hợp lệ
            Set<OrderStatus> allowedStatuses = VALID_TRANSITIONS.getOrDefault(order.getStatus(), Set.of());
            if (!allowedStatuses.contains(newStatus)) {
                throw new SystemException(SystemErrorCode.INVALID_ORDER_STATUS_TRANSITION);
            }

            order.setStatus(newStatus);
        } catch (IllegalArgumentException e) {
            throw new SystemException(SystemErrorCode.INVALID_PARAMETER);
        }

        orderRepository.save(order);

        // Gửi Notification cho khách hàng qua FCM và lưu vào DB
        String title = "Cập nhật đơn hàng #" + orderId.substring(0, 8);
        String body = getNotificationBodyForStatus(status.toUpperCase());
        
        User user = userRepository.findById(order.getUserId()).orElse(null);
        if (user != null) {
            AppNotification appNotification = AppNotification.builder()
                    .user(user)
                    .title(title)
                    .body(body)
                    .orderId(orderId)
                    .build();
            appNotificationRepository.save(appNotification);
        }

        notificationService.sendNotificationToUser(order.getUserId(), title, body, java.util.Map.of("orderId", orderId));
    }

    private String getNotificationBodyForStatus(String status) {
        return switch (status) {
            case "PAID" -> "Đơn hàng của bạn đã được thanh toán thành công!";
            case "PREPARING" -> "Nhà hàng đang chuẩn bị món ăn cho bạn. Vui lòng đợi nhé!";
            case "DELIVERING" -> "Đơn hàng đang trên đường giao đến bạn. Tài xế sẽ sớm liên hệ!";
            case "COMPLETED" -> "Đơn hàng đã giao thành công. Chúc bạn ngon miệng!";
            case "CANCELLED" -> "Đơn hàng của bạn đã bị hủy.";
            default -> "Trạng thái đơn hàng của bạn đã được cập nhật thành: " + status;
        };
    }

    @Override
    public Page<OrderResponse> getAllOrders(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (status != null && !status.isBlank()) {
            try {
                OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
                return orderRepository.findAllByStatus(orderStatus, pageable).map(orderMapper::mapToResponse);
            } catch (IllegalArgumentException e) {
                return Page.empty(pageable);
            }
        }
        return orderRepository.findAll(pageable).map(orderMapper::mapToResponse);
    }
}