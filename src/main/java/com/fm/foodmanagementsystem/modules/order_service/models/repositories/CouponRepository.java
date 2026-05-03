package com.fm.foodmanagementsystem.modules.order_service.models.repositories;

import com.fm.foodmanagementsystem.modules.order_service.models.entities.Coupon;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, String> {
    Optional<Coupon> findByCode(String code);
    boolean existsByCode(String code);

    /**
     * Cùng điều kiện với {@code getCouponByCode}: đang bật, chưa hết hạn, còn lượt dùng.
     */
    @Query("""
            SELECT c FROM Coupon c
            WHERE c.isActive = true
              AND c.expiresAt > :now
              AND (c.usageLimit IS NULL
                   OR (COALESCE(c.usedCount, 0) + COALESCE(c.reservedCount, 0) < c.usageLimit))
            ORDER BY c.expiresAt ASC
            """)
    List<Coupon> findAllAvailableForPublicDisplay(@Param("now") LocalDateTime now);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Coupon c WHERE c.code = :code")
    Optional<Coupon> findByCodeWithLock(@Param("code") String code);
}