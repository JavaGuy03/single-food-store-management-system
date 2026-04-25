package com.fm.foodmanagementsystem.modules.order_service.services.imps;

import com.fm.foodmanagementsystem.core.exception.SystemException;
import com.fm.foodmanagementsystem.core.exception.enums.SystemErrorCode;
import com.fm.foodmanagementsystem.modules.order_service.mappers.CouponMapper;
import com.fm.foodmanagementsystem.modules.order_service.models.entities.Coupon;
import com.fm.foodmanagementsystem.modules.order_service.models.repositories.CouponRepository;
import com.fm.foodmanagementsystem.modules.order_service.resources.requests.CouponRequest;
import com.fm.foodmanagementsystem.modules.order_service.resources.responses.CouponResponse;
import com.fm.foodmanagementsystem.modules.order_service.services.interfaces.ICouponService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CouponService implements ICouponService {

    CouponRepository couponRepository;
    CouponMapper couponMapper;

    @Override
    public CouponResponse createCoupon(CouponRequest request) {
        if (couponRepository.existsByCode(request.code())) throw new SystemException(SystemErrorCode.COUPON_ALREADY_EXISTS);

        Coupon coupon = Coupon.builder()
                .code(request.code().toUpperCase())
                .discountType(request.discountType())
                .discountValue(request.discountValue())
                .minOrderValue(request.minOrderValue())
                .maxDiscount(request.maxDiscount())
                .expiresAt(request.expiresAt())
                .usageLimit(request.usageLimit())
                .build();

        return couponMapper.mapToResponse(couponRepository.save(coupon));
    }

    @Override
    public CouponResponse getCouponByCode(String code) {
        Coupon coupon = couponRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new SystemException(SystemErrorCode.DATA_NOT_FOUND));
        return couponMapper.mapToResponse(coupon);
    }

    @Override
    public List<CouponResponse> getAllCoupons() {
        return couponRepository.findAll().stream()
                .map(couponMapper::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteCoupon(String id) {
        if (!couponRepository.existsById(id)) {
            throw new SystemException(SystemErrorCode.DATA_NOT_FOUND);
        }
        couponRepository.deleteById(id);
    }
}