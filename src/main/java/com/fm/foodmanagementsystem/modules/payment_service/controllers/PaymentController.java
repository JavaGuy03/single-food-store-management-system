package com.fm.foodmanagementsystem.modules.payment_service.controllers;

import com.fm.foodmanagementsystem.core.response.ApiResponse;
import com.fm.foodmanagementsystem.modules.payment_service.services.interfaces.IPaymentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentController {

    IPaymentService paymentService;

    @PostMapping("/zalopay/create")
    public ApiResponse<Map<String, Object>> createPayment(
            @RequestParam String orderId) {

        return ApiResponse.<Map<String, Object>>builder()
                .message("Tạo đơn thanh toán ZaloPay thành công")
                .result(paymentService.createZaloPayOrder(orderId))
                .build();
    }

    @PostMapping("/zalopay/query")
    public ApiResponse<Map<String, Object>> queryPayment(
            @RequestParam String appTransId) {

        return ApiResponse.<Map<String, Object>>builder()
                .message("Truy vấn trạng thái ZaloPay")
                .result(paymentService.queryZaloPayOrder(appTransId))
                .build();
    }
}