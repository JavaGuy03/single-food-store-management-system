package com.fm.foodmanagementsystem.modules.payment_service.models.repositories;

import com.fm.foodmanagementsystem.modules.payment_service.models.entities.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    Optional<PaymentTransaction> findByAppTransId(String appTransId);
    List<PaymentTransaction> findByStatus(String status);
    boolean existsByOrderIdAndStatus(String orderId, String status);
}
