package com.fm.foodmanagementsystem.modules.order_service.controllers;

import com.fm.foodmanagementsystem.core.response.ApiResponse;
import com.fm.foodmanagementsystem.modules.order_service.resources.requests.CouponRequest;
import com.fm.foodmanagementsystem.modules.order_service.resources.responses.CouponResponse;
import com.fm.foodmanagementsystem.modules.order_service.services.interfaces.ICouponService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CouponController {

    ICouponService couponService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CouponResponse> create(@RequestBody @Valid CouponRequest request) {
        return ApiResponse.<CouponResponse>builder()
                .message("Tạo mã giảm giá thành công")
                .result(couponService.createCoupon(request))
                .build();
    }

    // API này dành cho Khách hàng lúc nhập mã ở giỏ hàng (Không cần quyền Admin)
    @GetMapping("/{code}")
    public ApiResponse<CouponResponse> getByCode(@PathVariable String code) {
        return ApiResponse.<CouponResponse>builder()
                .result(couponService.getCouponByCode(code))
                .build();
    }

    // 👇 BỔ SUNG: API Lấy danh sách cho màn hình Admin
    @GetMapping
    public ApiResponse<List<CouponResponse>> getAllCoupons() {
        return ApiResponse.<List<CouponResponse>>builder()
                .result(couponService.getAllCoupons())
                .build();
    }

    // 👇 BỔ SUNG: API Xoá mã giảm giá
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteCoupon(@PathVariable String id) {
        couponService.deleteCoupon(id);
        return ApiResponse.<Void>builder()
                .message("Xóa mã giảm giá thành công")
                .build();
    }

    // 👇 BỔ SUNG: API Cập nhật mã giảm giá
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CouponResponse> updateCoupon(@PathVariable String id, @RequestBody @Valid CouponRequest request) {
        return ApiResponse.<CouponResponse>builder()
                .message("Cập nhật mã giảm giá thành công")
                .result(couponService.updateCoupon(id, request))
                .build();
    }
}