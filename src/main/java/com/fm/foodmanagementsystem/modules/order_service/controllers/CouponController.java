package com.fm.foodmanagementsystem.modules.order_service.controllers;

import com.fm.foodmanagementsystem.core.response.ApiResponse;
import com.fm.foodmanagementsystem.modules.order_service.resources.requests.CouponRequest;
import com.fm.foodmanagementsystem.modules.order_service.resources.responses.CouponResponse;
import com.fm.foodmanagementsystem.modules.order_service.services.interfaces.ICouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/coupons")
@Tag(name = "Coupons", description = """
        Mã giảm giá. Thời điểm hết hạn được đánh giá trên server bằng LocalDateTime.now() (timezone JVM).
        BA nên mô tả nghiệp vụ: hết hạn theo giờ cửa hàng / giờ hệ thống; app nên ghi chú khi hiển thị HSD để tránh tranh chấp
        “thấy trên danh sách nhưng nhập mã báo hết hạn”.
        """)
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

    @Operation(summary = "Danh sách mã đang áp dụng được (khách, public)",
            description = """
                    Chỉ trả các mã thỏa điều kiện **cùng lúc** với GET /{code}: đang bật, chưa hết hạn, còn lượt.
                    “Chưa hết hạn” = expiresAt > LocalDateTime.now() trên server (múi giờ JVM).

                    BA / UX: Nên ghi trên app rằng HSD theo giờ cửa hàng (hoặc giờ hệ thống) và hiển thị expiresAt rõ ràng;
                    có thể có vài giây giữa lúc xem danh sách và lúc áp mã — nếu đã quá HSD theo server thì API áp mã sẽ từ chối.
                    """)
    @GetMapping("/public-list")
    public ApiResponse<List<CouponResponse>> getPublicCoupons() {
        return ApiResponse.<List<CouponResponse>>builder()
                .result(couponService.getPublicCouponsForDisplay())
                .build();
    }

    // API Lấy danh sách cho màn hình Admin
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<CouponResponse>> getAllCoupons() {
        return ApiResponse.<List<CouponResponse>>builder()
                .result(couponService.getAllCoupons())
                .build();
    }

    @Operation(summary = "Tra cứu mã theo code (khách, khi nhập ở giỏ)",
            description = """
                    Kiểm tra mã có áp dụng được tại thời điểm gọi API: đang bật, expiresAt > LocalDateTime.now() (server), còn lượt.
                    Cùng quy tắc thời gian với GET /public-list — không phụ thuộc đồng hồ thiết bị khách.
                    """)
    @GetMapping("/{code}")
    public ApiResponse<CouponResponse> getByCode(@PathVariable String code) {
        return ApiResponse.<CouponResponse>builder()
                .result(couponService.getCouponByCode(code))
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