package com.fm.foodmanagementsystem.modules.payment_service.services.imps;

import com.fm.foodmanagementsystem.modules.payment_service.models.repositories.PaymentTransactionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cập nhật {@code payment_transactions} trong transaction độc lập —
 * để khi luồng query ZaloPay lỗi sau khi cổ báo {@code SUCCESS}, trạng thái txn vẫn được khóa
 * và không bị rollback cùng transaction ngoại vi.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentTransactionCommitService {

    PaymentTransactionRepository paymentTransactionRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStatusStandalone(String appTransId, String status, String zpTransId) {
        paymentTransactionRepository.findByAppTransId(appTransId).ifPresent(tx -> {
            tx.setStatus(status);
            if (zpTransId != null) {
                tx.setZpTransId(zpTransId);
            }
            paymentTransactionRepository.save(tx);
        });
    }
}
