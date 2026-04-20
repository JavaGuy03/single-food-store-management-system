package com.fm.foodmanagementsystem.modules.order_service.services.interfaces;

import com.fm.foodmanagementsystem.modules.order_service.resources.requests.CouponRequest;
import com.fm.foodmanagementsystem.modules.order_service.resources.responses.CouponResponse;

public interface ICouponService {
    CouponResponse createCoupon(CouponRequest request);
    CouponResponse getCouponByCode(String code);
}