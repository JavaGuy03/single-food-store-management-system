package com.fm.foodmanagementsystem.modules.order_service.services.interfaces;

import com.fm.foodmanagementsystem.modules.order_service.resources.requests.CouponRequest;
import com.fm.foodmanagementsystem.modules.order_service.resources.responses.CouponResponse;

import java.util.List;

public interface ICouponService {
    CouponResponse createCoupon(CouponRequest request);
    CouponResponse getCouponByCode(String code);
    CouponResponse updateCoupon(String id, CouponRequest request);
    List<CouponResponse> getAllCoupons();
    void deleteCoupon(String id);
}