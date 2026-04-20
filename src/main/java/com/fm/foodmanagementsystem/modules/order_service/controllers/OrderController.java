package com.fm.foodmanagementsystem.modules.order_service.controllers;

import com.fm.foodmanagementsystem.core.response.ApiResponse;
import com.fm.foodmanagementsystem.modules.order_service.resources.requests.OrderRequest;
import com.fm.foodmanagementsystem.modules.order_service.resources.responses.OrderResponse;
import com.fm.foodmanagementsystem.modules.order_service.services.interfaces.IOrderService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderController {

    IOrderService orderService;

    @PostMapping
    public ApiResponse<OrderResponse> create(@RequestBody @Valid OrderRequest request) {
        // Lấy email/username từ token đã đăng nhập
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.<OrderResponse>builder()
                .message("Đặt hàng thành công")
                .result(orderService.createOrder(userId, request))
                .build();
    }

    @GetMapping("/my-orders")
    public ApiResponse<List<OrderResponse>> getMyOrders() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.<List<OrderResponse>>builder()
                .result(orderService.getMyOrders(userId))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> getById(@PathVariable String id) {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.getOrderById(id))
                .build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> updateStatus(@PathVariable String id, @RequestParam String status) {
        orderService.updateOrderStatus(id, status);
        return ApiResponse.<Void>builder().message("Cập nhật trạng thái đơn hàng thành công").build();
    }
}