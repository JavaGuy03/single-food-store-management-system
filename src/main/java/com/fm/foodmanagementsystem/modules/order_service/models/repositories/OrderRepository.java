package com.fm.foodmanagementsystem.modules.order_service.models.repositories;

import com.fm.foodmanagementsystem.modules.order_service.models.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findAllByUserId(String userId);
}