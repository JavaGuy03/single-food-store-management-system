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
import com.fm.foodmanagementsystem.modules.setting_service.resources.responses.StoreSettingResponse;
import com.fm.foodmanagementsystem.modules.setting_service.services.interfaces.ISettingService;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
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
    ISettingService settingService;

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

        StoreSettingResponse store = settingService.getStoreSetting();
        if (Boolean.FALSE.equals(store.isOpen())) {
            throw new SystemException(SystemErrorCode.STORE_CLOSED);
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
                if (selectedOptions.size() != itemReq.selectedOptionIds().size()) {
                    throw new SystemException(SystemErrorCode.INVALID_PARAMETER);
                }
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

        double itemsSubtotal = totalAmount;
        double baseShipping = store.baseShippingFee() != null ? store.baseShippingFee() : 0.0;
        boolean freeShip = store.freeShipThreshold() != null && itemsSubtotal >= store.freeShipThreshold();
        double shippingFee = freeShip ? 0.0 : baseShipping;
        order.setShippingFee(shippingFee);

        double payableBeforeDiscount = itemsSubtotal + shippingFee;
        totalAmount = payableBeforeDiscount;

        // ÁP DỤNG KHOÁ PESSIMISTIC CHO COUPON (đơn tối thiểu theo tiền hàng; giảm giá trên tiền hàng + ship)
        if (request.couponCode() != null && !request.couponCode().trim().isEmpty()) {
            Coupon coupon = couponRepository.findByCodeWithLock(request.couponCode().toUpperCase())
                    .orElseThrow(() -> new SystemException(SystemErrorCode.DATA_NOT_FOUND));

            // M4: Dùng error code riêng cho từng trường hợp coupon lỗi
            if (!coupon.getIsActive() || coupon.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new SystemException(SystemErrorCode.COUPON_EXPIRED);
            }
            int committed = nz(coupon.getUsedCount()) + nz(coupon.getReservedCount());
            if (coupon.getUsageLimit() != null && committed >= coupon.getUsageLimit()) {
                throw new SystemException(SystemErrorCode.COUPON_USAGE_LIMIT);
            }
            if (coupon.getMinOrderValue() != null && itemsSubtotal < coupon.getMinOrderValue()) {
                throw new SystemException(SystemErrorCode.COUPON_MIN_ORDER);
            }

            double calculatedDiscount = 0.0;
            if ("PERCENTAGE".equalsIgnoreCase(coupon.getDiscountType())) {
                calculatedDiscount = payableBeforeDiscount * (coupon.getDiscountValue() / 100.0);
                if (coupon.getMaxDiscount() != null && calculatedDiscount > coupon.getMaxDiscount()) {
                    calculatedDiscount = coupon.getMaxDiscount();
                }
            } else if ("FIXED".equalsIgnoreCase(coupon.getDiscountType())) {
                calculatedDiscount = coupon.getDiscountValue();
            } else {
                throw new SystemException(SystemErrorCode.INVALID_PARAMETER);
            }

            double actualDiscount = Math.min(calculatedDiscount, payableBeforeDiscount);
            order.setDiscountAmount(actualDiscount);
            totalAmount = Math.max(0.0, payableBeforeDiscount - actualDiscount);

            coupon.setReservedCount(nz(coupon.getReservedCount()) + 1);
            couponRepository.save(coupon);
        } else {
            order.setDiscountAmount(0.0);
        }

        order.setOrderItems(orderItems);
        order.setItemsSummary(itemsSummary);
        order.setTotalAmount(totalAmount);
        if (request.couponCode() != null && !request.couponCode().trim().isEmpty()) {
            order.setCouponCode(request.couponCode().toUpperCase());
        }

        order = orderRepository.save(order);
        try {
            notificationService.sendNotificationToTopic("admin_orders", "Đơn hàng mới!", "Có đơn hàng mới cần xử lý.", java.util.Map.of("orderId", order.getId()));
        } catch (Exception e) {
            log.error("Failed to send notification to admin_orders topic: ", e);
        }

        // Pass already-fetched user to avoid N+1 inside mapper
        User orderUser = userRepository.findById(userId).orElse(null);
        return orderMapper.mapToResponse(order, orderUser);
    }

    @Override
    public List<OrderResponse> getMyOrders(String userId) {
        // Fetch user once, pass into all order mappings — avoids N+1 and "Không rõ" customer info
        User customer = userRepository.findById(userId).orElse(null);
        return orderRepository.findAllByUserId(userId).stream()
                .map(order -> orderMapper.mapToResponse(order, customer))
                .toList();
    }

    @Override
    public OrderResponse getOrderById(String id, String callerId) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new SystemException(SystemErrorCode.DATA_NOT_FOUND));

        // Ownership check: Khách chỉ xem đơn của mình, Admin xem tất cả
        boolean isAdmin = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !order.getUserId().equals(callerId)) {
            throw new SystemException(SystemErrorCode.UNAUTHORIZED_ACTION);
        }

        User user = userRepository.findById(order.getUserId()).orElse(null);
        return orderMapper.mapToResponse(order, user);
    }

    @Override
    @Transactional
    public void cancelOrder(String userId, String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new SystemException(SystemErrorCode.DATA_NOT_FOUND));

        if (!order.getUserId().equals(userId)) {
            throw new SystemException(SystemErrorCode.UNAUTHORIZED_ACTION);
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new SystemException(SystemErrorCode.INVALID_ORDER_STATUS_TRANSITION);
        }

        order.setStatus(OrderStatus.CANCELLED);
        if (order.getCouponCode() != null) {
            releaseCouponReservation(order.getCouponCode());
        }

        orderRepository.save(order);

        try {
            User customer = userRepository.findById(order.getUserId()).orElse(null);
            if (customer != null) {
                String ref = shortOrderRef(orderId);
                appNotificationRepository.save(AppNotification.builder()
                        .user(customer)
                        .title("Đơn hàng #" + ref + " đã hủy")
                        .body("Bạn đã hủy đơn hàng thành công.")
                        .orderId(orderId)
                        .build());
                notificationService.sendNotificationToUser(order.getUserId(),
                        "Đơn hàng #" + ref + " đã hủy",
                        "Bạn đã hủy đơn hàng thành công.",
                        java.util.Map.of("orderId", orderId));
            }
        } catch (Exception e) {
            log.error("Failed to notify customer on order cancel: ", e);
        }

        try {
            notificationService.sendNotificationToTopic("admin_orders", "Đơn hàng bị huỷ!", "Đơn hàng #" + shortOrderRef(orderId) + " đã bị huỷ bởi khách hàng.", java.util.Map.of("orderId", orderId));
        } catch (Exception e) {
            log.error("Failed to send cancellation notification to admin_orders topic: ", e);
        }
    }

    // M3: Validate trạng thái chuyển đổi hợp lệ
    @Override
    @Transactional
    public void updateOrderStatus(String orderId, String status) {
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new SystemException(SystemErrorCode.DATA_NOT_FOUND));

        try {
            OrderStatus newStatus = OrderStatus.valueOf(status.toUpperCase());
            OrderStatus previousStatus = order.getStatus();

            // Hai luồng thanh toán/query cùng lúc: luồng sau thấy PAID và thoát không ném INVALID_TRANSITION.
            if (previousStatus == newStatus) {
                return;
            }

            // Kiểm tra luồng chuyển trạng thái hợp lệ
            Set<OrderStatus> allowedStatuses = VALID_TRANSITIONS.getOrDefault(previousStatus, Set.of());
            if (!allowedStatuses.contains(newStatus)) {
                throw new SystemException(SystemErrorCode.INVALID_ORDER_STATUS_TRANSITION);
            }

            if (newStatus == OrderStatus.PAID && previousStatus == OrderStatus.PENDING && order.getCouponCode() != null) {
                consumeCouponOnPaid(order.getCouponCode());
            }

            order.setStatus(newStatus);

            if (newStatus == OrderStatus.CANCELLED && order.getCouponCode() != null) {
                if (previousStatus == OrderStatus.PENDING) {
                    releaseCouponReservation(order.getCouponCode());
                } else if (couponUsageCommitted(previousStatus)) {
                    refundCouponPaidUsage(order.getCouponCode());
                }
            }

        } catch (IllegalArgumentException e) {
            throw new SystemException(SystemErrorCode.INVALID_PARAMETER);
        }

        orderRepository.save(order);

        // Báo cho Admin nếu đơn hàng vừa chuyển sang PAID (thanh toán thành công)
        if (order.getStatus() == OrderStatus.PAID) {
            try {
                notificationService.sendNotificationToTopic("admin_orders", "Đơn hàng đã thanh toán!", "Đơn hàng #" + shortOrderRef(orderId) + " vừa được thanh toán thành công.", java.util.Map.of("orderId", orderId));
            } catch (Exception e) {
                log.error("Failed to send PAID notification to admin_orders topic: ", e);
            }
        }

        // Gửi Notification cho khách hàng qua FCM và lưu vào DB
        String title = "Cập nhật đơn hàng #" + shortOrderRef(orderId);
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

    /** Coupon đã được tính used sau khi thanh toán (PAID). */
    private static boolean couponUsageCommitted(OrderStatus status) {
        return status == OrderStatus.PAID
                || status == OrderStatus.PREPARING
                || status == OrderStatus.DELIVERING
                || status == OrderStatus.COMPLETED;
    }

    private static int nz(Integer v) {
        return v != null ? v : 0;
    }

    private static String shortOrderRef(String orderId) {
        if (orderId == null || orderId.isEmpty()) {
            return "?";
        }
        return orderId.substring(0, Math.min(8, orderId.length()));
    }

    /** Huỷ slot đặt trước khi đơn vẫn PENDING (khách huỷ hoặc admin huỷ từ PENDING). */
    private void releaseCouponReservation(String couponCode) {
        couponRepository.findByCodeWithLock(couponCode).ifPresent(coupon -> {
            int r = nz(coupon.getReservedCount());
            if (r > 0) {
                coupon.setReservedCount(r - 1);
                couponRepository.save(coupon);
            }
        });
    }

    /** Chuyển giữ chỗ → đã dùng sau thanh toán (PAID). */
    private void consumeCouponOnPaid(String couponCode) {
        Coupon coupon = couponRepository.findByCodeWithLock(couponCode)
                .orElseThrow(() -> new SystemException(SystemErrorCode.DATA_NOT_FOUND));
        if (!coupon.getIsActive() || coupon.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new SystemException(SystemErrorCode.COUPON_EXPIRED);
        }
        int reserved = nz(coupon.getReservedCount());
        if (reserved > 0) {
            coupon.setReservedCount(reserved - 1);
        }
        int used = nz(coupon.getUsedCount());
        if (coupon.getUsageLimit() != null && used >= coupon.getUsageLimit()) {
            throw new SystemException(SystemErrorCode.COUPON_USAGE_LIMIT);
        }
        coupon.setUsedCount(used + 1);
        couponRepository.save(coupon);
    }

    /** Hoàn lượt đã thanh toán khi huỷ đơn sau PAID. */
    private void refundCouponPaidUsage(String couponCode) {
        couponRepository.findByCodeWithLock(couponCode).ifPresent(coupon -> {
            if (nz(coupon.getUsedCount()) > 0) {
                coupon.setUsedCount(nz(coupon.getUsedCount()) - 1);
                couponRepository.save(coupon);
            }
        });
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

        // Bước 1: Phân trang ở DB (không có @EntityGraph → không HHH90003004)
        Page<Order> stubPage;
        if (status != null && !status.isBlank()) {
            try {
                OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
                stubPage = orderRepository.findAllByStatus(orderStatus, pageable);
            } catch (IllegalArgumentException e) {
                throw new SystemException(SystemErrorCode.INVALID_PARAMETER);
            }
        } else {
            stubPage = orderRepository.findAll(pageable);
        }

        if (stubPage.isEmpty()) {
            return stubPage.map(o -> orderMapper.mapToResponse(o, null));
        }

        // Bước 2: Load orderItems + food chỉ cho tập nhỏ (= page size) theo IDs
        List<String> pageIds = stubPage.stream().map(Order::getId).toList();
        Map<String, Order> fullOrders = orderRepository.findWithItemsByIds(pageIds).stream()
                .collect(Collectors.toMap(Order::getId, o -> o));

        // Batch-fetch users — tránh N+1 và thông tin "Không rõ"
        Set<String> userIds = stubPage.stream()
                .map(Order::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<String, User> usersById = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        // Giữ thứ tự pagination gốc khi tạo response
        List<OrderResponse> responses = stubPage.getContent().stream()
                .map(stub -> fullOrders.getOrDefault(stub.getId(), stub))
                .map(order -> orderMapper.mapToResponse(order, usersById.get(order.getUserId())))
                .toList();

        return new PageImpl<>(responses, pageable, stubPage.getTotalElements());
    }
}