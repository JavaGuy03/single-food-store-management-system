package com.fm.foodmanagementsystem.modules.order_service.models.repositories;

import com.fm.foodmanagementsystem.modules.order_service.models.entities.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findAllByOrderId(String orderId);
}