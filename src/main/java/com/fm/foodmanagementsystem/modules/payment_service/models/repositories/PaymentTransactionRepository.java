package com.fm.foodmanagementsystem.modules.payment_service.models.repositories;

import com.fm.foodmanagementsystem.modules.payment_service.models.entities.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
}
