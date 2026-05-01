package com.fm.foodmanagementsystem.modules.payment_service.services.imps;

import com.fm.foodmanagementsystem.core.exception.SystemException;
import com.fm.foodmanagementsystem.core.exception.enums.SystemErrorCode;
import com.fm.foodmanagementsystem.modules.order_service.services.interfaces.IOrderService;
import com.fm.foodmanagementsystem.modules.payment_service.configs.ZaloPayConfig;
import com.fm.foodmanagementsystem.modules.order_service.models.repositories.OrderRepository;
import com.fm.foodmanagementsystem.modules.payment_service.models.entities.PaymentTransaction;
import com.fm.foodmanagementsystem.modules.payment_service.models.repositories.PaymentTransactionRepository;
import com.fm.foodmanagementsystem.modules.payment_service.services.interfaces.IPaymentService;
import com.fm.foodmanagementsystem.modules.payment_service.utils.HmacUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentService implements IPaymentService {

    ZaloPayConfig zaloPayConfig;
    IOrderService orderService;
    OrderRepository orderRepository;
    PaymentTransactionRepository paymentTransactionRepository;

    // C4: Đánh dấu @NonFinal để Lombok không yêu cầu inject qua constructor
    @NonFinal
    RestTemplate restTemplate = new RestTemplate();

    @Override
    public Map<String, Object> createZaloPayOrder(String orderId) {
        double amount = orderRepository.findById(orderId)
                .orElseThrow(() -> new SystemException(SystemErrorCode.DATA_NOT_FOUND))
                .getTotalAmount();
        String appTransId = getCurrentTimeString("yyMMdd") + "_" + System.currentTimeMillis();

        Map<String, Object> order = new HashMap<>();
        order.put("app_id", zaloPayConfig.getAppId());
        order.put("app_trans_id", appTransId);
        order.put("app_time", System.currentTimeMillis());
        order.put("app_user", "FoodManager");
        order.put("amount", (long) amount);
        order.put("description", "Thanh toan don hang #" + orderId);
        order.put("bank_code", "");
        order.put("item", "[]");
        order.put("embed_data", "{\"orderId\": \"" + orderId + "\"}"); // Gắn kèm orderId để lúc Query còn biết đồng mà
                                                                       // cập nhật
        order.put("callback_url", zaloPayConfig.getCallbackUrl());

        String data = order.get("app_id") + "|" + order.get("app_trans_id") + "|" + order.get("app_user") + "|"
                + order.get("amount")
                + "|" + order.get("app_time") + "|" + order.get("embed_data") + "|" + order.get("item");

        order.put("mac", HmacUtil.HMacHexStringEncode(HmacUtil.HMACSHA256, zaloPayConfig.getKey1(), data));

        // Gửi API ZaloPay
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        order.forEach((key, value) -> body.add(key, String.valueOf(value)));

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            Map<String, Object> response = restTemplate.postForObject(zaloPayConfig.getCreateOrderEndpoint(), request,
                    Map.class);
            if (response != null) {
                response.put("app_trans_id", appTransId); // Trả kèm cái này về cho Mobile

                String userId = SecurityContextHolder.getContext().getAuthentication().getName();
                PaymentTransaction transaction = PaymentTransaction.builder()
                        .orderId(orderId)
                        .userId(userId)
                        .amount(amount)
                        .paymentMethod("ZALOPAY")
                        .appTransId(appTransId)
                        .status("PENDING")
                        .build();
                paymentTransactionRepository.save(transaction);
            }
            return response;
        } catch (Exception e) {
            log.error("Lỗi gọi API ZaloPay: ", e);
            throw new SystemException(SystemErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    // M9: Hoàn thiện logic cập nhật trạng thái đơn hàng khi thanh toán thành công
    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public Map<String, Object> queryZaloPayOrder(String appTransId) {
        String data = zaloPayConfig.getAppId() + "|" + appTransId + "|" + zaloPayConfig.getKey1();
        String mac = HmacUtil.HMacHexStringEncode(HmacUtil.HMACSHA256, zaloPayConfig.getKey1(), data);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("app_id", zaloPayConfig.getAppId());
        body.add("app_trans_id", appTransId);
        body.add("mac", mac);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            Map<String, Object> response = restTemplate.postForObject(zaloPayConfig.getQueryEndpoint(), request,
                    Map.class);

            if (response != null && response.containsKey("return_code")) {
                int returnCode = (Integer) response.get("return_code");

                if (returnCode == 1) {
                    // Thanh toán THÀNH CÔNG!
                    log.info("Giao dịch {} THÀNH CÔNG! Đang cập nhật trạng thái đơn hàng...", appTransId);

                    // Rút orderId từ embed_data
                    String orderId = extractOrderIdFromTransaction(appTransId);

                    if (orderId != null) {
                        orderService.updateOrderStatus(orderId, "PAID");
                        String zpTransId = response.containsKey("zp_trans_id")
                                ? String.valueOf(response.get("zp_trans_id"))
                                : null;
                        updateTransactionStatus(appTransId, "SUCCESS", zpTransId);
                        response.put("order_status_update", "SUCCESS");
                        log.info("Đã cập nhật đơn hàng {} sang trạng thái PAID", orderId);
                    } else {
                        log.warn("Không tìm thấy orderId cho giao dịch {}", appTransId);
                        response.put("order_status_update", "ORDER_NOT_FOUND");
                    }
                } else if (returnCode == 2) {
                    // Thanh toán THẤT BẠI
                    updateTransactionStatus(appTransId, "FAILED", null);
                }
            }
            return response;
        } catch (Exception e) {
            log.error("Lỗi truy vấn ZaloPay: ", e);
            throw new SystemException(SystemErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    // Helper: Tìm orderId từ PaymentTransaction đã lưu
    private String extractOrderIdFromTransaction(String appTransId) {
        return paymentTransactionRepository.findByAppTransId(appTransId)
                .map(PaymentTransaction::getOrderId)
                .orElse(null);
    }

    private void updateTransactionStatus(String appTransId, String status, String zpTransId) {
        paymentTransactionRepository.findByAppTransId(appTransId).ifPresent(tx -> {
            tx.setStatus(status);
            if (zpTransId != null) {
                tx.setZpTransId(zpTransId);
            }
            paymentTransactionRepository.save(tx);
        });
    }

    // CRON JOB: Tự động check các giao dịch PENDING mỗi 2 phút
    @Scheduled(fixedDelay = 120000)
    @Transactional
    public void pollPendingTransactions() {
        List<PaymentTransaction> pendingTx = paymentTransactionRepository.findByStatus("PENDING");
        if (!pendingTx.isEmpty()) {
            log.info("CronJob: Đang kiểm tra {} giao dịch ZaloPay PENDING...", pendingTx.size());
            for (PaymentTransaction tx : pendingTx) {
                // Tránh query các giao dịch mới tạo trong vòng 1 phút qua
                if (tx.getCreatedAt().plusMinutes(1).isBefore(java.time.LocalDateTime.now())) {
                    queryZaloPayOrder(tx.getAppTransId());
                }

                // Hủy giao dịch nếu treo quá 15 phút
                if (tx.getCreatedAt().plusMinutes(15).isBefore(java.time.LocalDateTime.now())) {
                    updateTransactionStatus(tx.getAppTransId(), "FAILED", null);
                }
            }
        }
    }

    private String getCurrentTimeString(String format) {
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT+7"));
        SimpleDateFormat fmt = new SimpleDateFormat(format);
        fmt.setCalendar(cal);
        return fmt.format(cal.getTimeInMillis());
    }
}