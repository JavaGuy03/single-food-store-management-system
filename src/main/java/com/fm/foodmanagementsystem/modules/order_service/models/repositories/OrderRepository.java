package com.fm.foodmanagementsystem.modules.order_service.models.repositories;

import com.fm.foodmanagementsystem.modules.order_service.models.entities.Order;
import com.fm.foodmanagementsystem.modules.order_service.models.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    // M-3 + M-6 FIX: Eagerly fetch orderItems + food to prevent N+1 and LazyInitializationException
    @EntityGraph(attributePaths = {"orderItems", "orderItems.food"})
    List<Order> findAllByUserId(String userId);

    @EntityGraph(attributePaths = {"orderItems", "orderItems.food"})
    java.util.Optional<Order> findById(String id);

    /** Khóa dòng đơn để tránh hai luồng cùng chuyển PENDING→PAID (coupon / trạng thái). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    java.util.Optional<Order> findByIdForUpdate(@Param("id") String id);

    @EntityGraph(attributePaths = {"orderItems", "orderItems.food"})
    Page<Order> findAllByStatus(OrderStatus status, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"orderItems", "orderItems.food"})
    Page<Order> findAll(Pageable pageable);

    boolean existsByUserIdAndStatusIn(String userId, Collection<OrderStatus> statuses);

    boolean existsByCouponCodeAndStatus(String couponCode, OrderStatus status);

    // Đếm số đơn trong ngày (không tính đơn Hủy)
    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderDate >= :startOfDay AND o.orderDate <= :endOfDay AND o.status != 'CANCELLED'")
    long countOrdersByDateRange(@Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);

    // Tính tổng doanh thu trong ngày (Chỉ tính đơn đã Giao thành công - COMPLETED)
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.orderDate >= :startOfDay AND o.orderDate <= :endOfDay AND o.status = 'COMPLETED'")
    Double sumRevenueByDateRange(@Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);

    // Lấy danh sách đơn hàng hoàn thành trong khoảng thời gian để xuất Excel
    @Query("SELECT o FROM Order o WHERE o.orderDate >= :startDate AND o.orderDate <= :endDate AND o.status = 'COMPLETED' ORDER BY o.orderDate DESC")
    List<Order> findCompletedOrdersForReport(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}