package com.fm.foodmanagementsystem.modules.order_service.services.interfaces;

import com.fm.foodmanagementsystem.modules.order_service.resources.requests.CouponRequest;
import com.fm.foodmanagementsystem.modules.order_service.resources.responses.CouponResponse;

import java.util.List;

public interface ICouponService {
    CouponResponse createCoupon(CouponRequest request);
    /**
     * Tra cứu mã: hết hạn nếu {@code expiresAt.isBefore(LocalDateTime.now())} — {@code now()} là giờ máy chủ (timezone JVM).
     */
    CouponResponse getCouponByCode(String code);

    /**
     * Danh sách mã đang áp dụng được (payload public, không lộ nội bộ). Lọc hết hạn dùng cùng {@code LocalDateTime.now()} với
     * {@link #getCouponByCode(String)} — BA nên quy ước “giờ cửa hàng” với cấu hình timezone server.
     */
    List<CouponResponse> getPublicCouponsForDisplay();

    CouponResponse updateCoupon(String id, CouponRequest request);
    List<CouponResponse> getAllCoupons();
    void deleteCoupon(String id);
}