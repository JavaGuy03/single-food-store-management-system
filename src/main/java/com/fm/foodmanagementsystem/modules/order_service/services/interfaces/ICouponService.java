package com.fm.foodmanagementsystem.modules.order_service.services.interfaces;

import com.fm.foodmanagementsystem.modules.order_service.resources.requests.CouponRequest;
import com.fm.foodmanagementsystem.modules.order_service.resources.responses.CouponResponse;

import java.util.List;

public interface ICouponService {
    CouponResponse createCoupon(CouponRequest request);
    CouponResponse getCouponByCode(String code);

    /** Danh sách mã đang áp dụng được (không lộ số liệu nội bộ) — dùng cho app khách. */
    List<CouponResponse> getPublicCouponsForDisplay();

    CouponResponse updateCoupon(String id, CouponRequest request);
    List<CouponResponse> getAllCoupons();
    void deleteCoupon(String id);
}