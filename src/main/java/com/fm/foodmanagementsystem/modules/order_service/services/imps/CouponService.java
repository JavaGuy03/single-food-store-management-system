package com.fm.foodmanagementsystem.modules.order_service.services.imps;

import com.fm.foodmanagementsystem.core.exception.SystemException;
import com.fm.foodmanagementsystem.core.exception.enums.SystemErrorCode;
import com.fm.foodmanagementsystem.modules.order_service.mappers.CouponMapper;
import com.fm.foodmanagementsystem.modules.order_service.models.entities.Coupon;
import com.fm.foodmanagementsystem.modules.order_service.models.enums.OrderStatus;
import com.fm.foodmanagementsystem.modules.order_service.models.repositories.CouponRepository;
import com.fm.foodmanagementsystem.modules.order_service.models.repositories.OrderRepository;
import com.fm.foodmanagementsystem.modules.order_service.resources.requests.CouponRequest;
import com.fm.foodmanagementsystem.modules.order_service.resources.responses.CouponResponse;
import com.fm.foodmanagementsystem.modules.order_service.services.interfaces.ICouponService;
import org.springframework.transaction.annotation.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CouponService implements ICouponService {

    CouponRepository couponRepository;
    OrderRepository orderRepository;
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
                .isActive(request.isActive() != null ? request.isActive() : true)
                .build();

        return couponMapper.mapToResponse(couponRepository.save(coupon));
    }

    @Override
    public CouponResponse getCouponByCode(String code) {
        Coupon coupon = couponRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new SystemException(SystemErrorCode.DATA_NOT_FOUND));

        // Validate: khách không nên thấy coupon hết hạn hoặc bị tắt
        if (!coupon.getIsActive() || coupon.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            throw new SystemException(SystemErrorCode.COUPON_EXPIRED);
        }
        int used = coupon.getUsedCount() != null ? coupon.getUsedCount() : 0;
        int reserved = coupon.getReservedCount() != null ? coupon.getReservedCount() : 0;
        if (coupon.getUsageLimit() != null && used + reserved >= coupon.getUsageLimit()) {
            throw new SystemException(SystemErrorCode.COUPON_USAGE_LIMIT);
        }

        return toPublicCouponDisplay(coupon);
    }

    @Override
    public List<CouponResponse> getPublicCouponsForDisplay() {
        LocalDateTime now = LocalDateTime.now();
        return couponRepository.findAllAvailableForPublicDisplay(now).stream()
                .map(this::toPublicCouponDisplay)
                .toList();
    }

    /** Giống payload {@link #getCouponByCode} — không trả usageLimit/usedCount/isActive cho khách. */
    private CouponResponse toPublicCouponDisplay(Coupon coupon) {
        return CouponResponse.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .minOrderValue(coupon.getMinOrderValue())
                .maxDiscount(coupon.getMaxDiscount())
                .expiresAt(coupon.getExpiresAt())
                .usageLimit(null)
                .usedCount(null)
                .reservedCount(null)
                .isActive(null)
                .build();
    }

    @Override
    @Transactional
    public CouponResponse updateCoupon(String id, CouponRequest request) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new SystemException(SystemErrorCode.DATA_NOT_FOUND));

        if (!coupon.getCode().equalsIgnoreCase(request.code()) && couponRepository.existsByCode(request.code())) {
            throw new SystemException(SystemErrorCode.COUPON_ALREADY_EXISTS);
        }

        coupon.setCode(request.code().toUpperCase());
        coupon.setDiscountType(request.discountType());
        coupon.setDiscountValue(request.discountValue());
        coupon.setMinOrderValue(request.minOrderValue());
        coupon.setMaxDiscount(request.maxDiscount());
        coupon.setExpiresAt(request.expiresAt());
        coupon.setUsageLimit(request.usageLimit());
        if (request.isActive() != null) {
            coupon.setIsActive(request.isActive());
        }

        int used = coupon.getUsedCount() != null ? coupon.getUsedCount() : 0;
        int reserved = coupon.getReservedCount() != null ? coupon.getReservedCount() : 0;
        if (coupon.getUsageLimit() != null && coupon.getUsageLimit() < used + reserved) {
            throw new SystemException(SystemErrorCode.INVALID_PARAMETER);
        }

        return couponMapper.mapToResponse(couponRepository.save(coupon));
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
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new SystemException(SystemErrorCode.DATA_NOT_FOUND));

        int reserved = coupon.getReservedCount() != null ? coupon.getReservedCount() : 0;
        if (reserved > 0 || orderRepository.existsByCouponCodeAndStatus(coupon.getCode(), OrderStatus.PENDING)) {
            throw new SystemException(SystemErrorCode.DATA_IS_IN_USE);
        }

        couponRepository.delete(coupon);
    }
}