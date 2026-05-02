package com.fm.foodmanagementsystem.modules.payment_service.services.imps;

import com.fm.foodmanagementsystem.core.exception.SystemException;
import com.fm.foodmanagementsystem.core.exception.enums.SystemErrorCode;
import com.fm.foodmanagementsystem.modules.order_service.services.interfaces.IOrderService;
import com.fm.foodmanagementsystem.modules.payment_service.configs.ZaloPayConfig;
import com.fm.foodmanagementsystem.modules.order_service.models.entities.Order;
import com.fm.foodmanagementsystem.modules.order_service.models.entities.OrderItem;
import com.fm.foodmanagementsystem.modules.order_service.models.enums.OrderStatus;
import com.fm.foodmanagementsystem.modules.order_service.models.repositories.OrderRepository;
import com.fm.foodmanagementsystem.modules.payment_service.models.entities.PaymentTransaction;
import com.fm.foodmanagementsystem.modules.payment_service.models.repositories.PaymentTransactionRepository;
import com.fm.foodmanagementsystem.modules.payment_service.services.interfaces.IPaymentService;
import com.fm.foodmanagementsystem.modules.payment_service.utils.HmacUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Thanh toán ZaloPay: <strong>không phụ thuộc callback</strong> (callback URL thường không gọi được từ môi trường dev/ngrok).
 * Luồng đúng:
 * <ul>
 *   <li>App gọi {@link #createZaloPayOrder} → lưu {@link PaymentTransaction} PENDING + {@code app_trans_id}</li>
 *   <li>Sau khi user thanh toán trên app ZaloPay, app <strong>bắt buộc gọi</strong> {@link #queryZaloPayOrder} với {@code app_trans_id} (hoặc đợi cron server, tối đa ~2 phút sau 1 phút)</li>
 *   <li>{@link #pollPendingTransactions} chạy định kỳ, gọi query cho mọi giao dịch PENDING (không JWT → bỏ qua kiểm tra chủ giao dịch)</li>
 * </ul>
 */
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
        // Read caller identity from JWT
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String callerId = jwt.getClaimAsString("user-id");

        var dbOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new SystemException(SystemErrorCode.DATA_NOT_FOUND));

        // Ownership check: only the order owner can initiate payment
        if (!dbOrder.getUserId().equals(callerId)) {
            throw new SystemException(SystemErrorCode.UNAUTHORIZED_ACTION);
        }

        // Chỉ cho thanh toán đơn PENDING — không thanh toán lại đơn đã PAID/CANCELLED
        if (dbOrder.getStatus() != com.fm.foodmanagementsystem.modules.order_service.models.enums.OrderStatus.PENDING) {
            throw new SystemException(SystemErrorCode.INVALID_ORDER_STATUS_TRANSITION);
        }

        // Chặn tạo thanh toán trùng — nếu đã có transaction PENDING cho đơn này
        boolean hasPendingTx = paymentTransactionRepository.existsByOrderIdAndStatus(orderId, "PENDING");
        if (hasPendingTx) {
            throw new SystemException(SystemErrorCode.DATA_IS_IN_USE);
        }

        long amountVnd = resolveAndSyncChargeAmountVnd(dbOrder);
        double amountForTx = amountVnd;
        String appTransId = getCurrentTimeString("yyMMdd") + "_" + System.currentTimeMillis();

        // ZaloPay: tổng item phải khớp amount — tránh item=[] khiến app chỉ hiển thị giá niêm yết / sai số tiền
        String itemJson = String.format(Locale.US,
                "[{\"itemid\":\"%s\",\"itemname\":\"Thanh toan don hang\",\"itemprice\":%d,\"itemquantity\":1}]",
                orderId.replace("\"", ""),
                amountVnd);
        String embedData = String.format(Locale.US, "{\"orderId\":\"%s\",\"amount\":%d}",
                orderId.replace("\"", ""),
                amountVnd);

        Map<String, Object> order = new HashMap<>();
        order.put("app_id", zaloPayConfig.getAppId());
        order.put("app_trans_id", appTransId);
        order.put("app_time", System.currentTimeMillis());
        order.put("app_user", "FoodManager");
        order.put("amount", amountVnd);
        order.put("description", "Thanh toan don hang #" + orderId.substring(0, Math.min(8, orderId.length())) + " - "
                + amountVnd + " VND");
        order.put("bank_code", "");
        order.put("item", itemJson);
        order.put("embed_data", embedData);
        // ZaloPay có thể bắt buộc trường này; đồng bộ đơn PAID không dựa vào callback — chỉ dùng query + cron.
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
                response.put("amount", amountVnd); // Số tiền thực thu (sau giảm giá / ship) — mobile nên hiển thị theo đây

                PaymentTransaction transaction = PaymentTransaction.builder()
                        .orderId(orderId)
                        .userId(callerId) // Already retrieved above
                        .amount(amountForTx)
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
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @SuppressWarnings("unchecked")
    public Map<String, Object> queryZaloPayOrder(String appTransId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            PaymentTransaction existingTx = paymentTransactionRepository.findByAppTransId(appTransId)
                    .orElseThrow(() -> new SystemException(SystemErrorCode.DATA_NOT_FOUND));
            String callerId = jwt.getClaimAsString("user-id");
            if (!existingTx.getUserId().equals(callerId)) {
                throw new SystemException(SystemErrorCode.UNAUTHORIZED_ACTION);
            }
        }

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
                int returnCode = parseZaloPayReturnCode(response.get("return_code"));

                if (returnCode == 1) {
                    // Thanh toán THÀNH CÔNG!
                    log.info("Giao dịch {} THÀNH CÔNG! Đang cập nhật trạng thái đơn hàng...", appTransId);

                    String orderId = extractOrderIdFromTransaction(appTransId);
                    String zpTransId = response.containsKey("zp_trans_id")
                            ? String.valueOf(response.get("zp_trans_id"))
                            : null;

                    if (orderId != null) {
                        Optional<Order> orderOpt = orderRepository.findById(orderId);
                        if (orderOpt.isPresent() && orderOpt.get().getStatus() == OrderStatus.PAID) {
                            // Idempotent: khách/cron query lại sau khi đơn đã PAID — tránh ném INVALID_TRANSITION và treo tx PENDING
                            updateTransactionStatus(appTransId, "SUCCESS", zpTransId);
                            response.put("order_status_update", "SUCCESS");
                            log.info("Đơn {} đã PAID — khóa tx {} SUCCESS (idempotent query)", orderId, appTransId);
                        } else if (orderOpt.isPresent() && orderOpt.get().getStatus() == OrderStatus.CANCELLED) {
                            // Khách huỷ đơn trong lúc đang thanh toán — tiền có thể đã vào ZaloPay nhưng không được PAID đơn
                            log.error(
                                    "orderId={}: ZaloPay báo thành công nhưng đơn đã CANCELLED — cần hoàn tiền / đối soát thủ công. appTransId={}",
                                    orderId,
                                    appTransId);
                            updateTransactionStatus(appTransId, "SUCCESS", zpTransId);
                            response.put("order_status_update", "ORDER_CANCELLED_PAYMENT_RECEIVED");
                        } else if (orderOpt.isEmpty()) {
                            log.warn("Không tìm thấy orderId={} cho giao dịch {}", orderId, appTransId);
                            response.put("order_status_update", "ORDER_NOT_FOUND");
                        } else {
                            try {
                                orderService.updateOrderStatus(orderId, "PAID");
                            } catch (SystemException e) {
                                log.error(
                                        "orderId={}: ZaloPay báo thành công nhưng không chuyển PAID được — kiểm tra coupon/hạn mức/ghi đơn thủ công. errorCode={}",
                                        orderId,
                                        e.getErrorCode());
                                throw e;
                            }
                            updateTransactionStatus(appTransId, "SUCCESS", zpTransId);
                            response.put("order_status_update", "SUCCESS");
                            log.info("Đã cập nhật đơn hàng {} sang trạng thái PAID", orderId);
                        }
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
    /** API ZaloPay có thể trả {@code return_code} kiểu Integer/Long/BigDecimal tùy client JSON. */
    private static int parseZaloPayReturnCode(Object raw) {
        if (raw instanceof Number n) {
            return n.intValue();
        }
        if (raw instanceof String s) {
            try {
                return Integer.parseInt(s.trim());
            } catch (NumberFormatException ignored) {
                return Integer.MIN_VALUE;
            }
        }
        return Integer.MIN_VALUE;
    }

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
    public void pollPendingTransactions() {
        List<PaymentTransaction> pendingTx = paymentTransactionRepository.findByStatus("PENDING");
        if (!pendingTx.isEmpty()) {
            log.info("CronJob: Đang kiểm tra {} giao dịch ZaloPay PENDING...", pendingTx.size());
            for (PaymentTransaction tx : pendingTx) {
                try {
                    java.time.LocalDateTime now = java.time.LocalDateTime.now();

                    // Hủy giao dịch nếu treo quá 15 phút (kiểm tra TRUỚC để tránh query ZaloPay cho tx đã chết)
                    if (tx.getCreatedAt().plusMinutes(15).isBefore(now)) {
                        log.warn("CronJob: Giao dịch {} đã quá 15 phút, đánh dấu FAILED", tx.getAppTransId());
                        updateTransactionStatus(tx.getAppTransId(), "FAILED", null);
                    } else if (tx.getCreatedAt().plusMinutes(1).isBefore(now)) {
                        // Chỉ query ZaloPay nếu giao dịch > 1 phút và chưa quá 15 phút
                        queryZaloPayOrder(tx.getAppTransId());
                    }
                } catch (Exception e) {
                    log.error("CronJob: Lỗi khi kiểm tra giao dịch {}: {}", tx.getAppTransId(), e.getMessage());
                }
            }
        }
    }

    private static double nz(Double v) {
        return v != null ? v : 0.0;
    }

    /**
     * Tiền phải thu = tổng dòng + ship - giảm giá (đồng bộ với logic OrderService).
     */
    private double computePayableFromOrderLines(Order o) {
        double items = 0.0;
        if (o.getOrderItems() != null) {
            for (OrderItem line : o.getOrderItems()) {
                items += line.getQuantity() * line.getUnitPrice();
            }
        }
        return Math.max(0.0, items + nz(o.getShippingFee()) - nz(o.getDiscountAmount()));
    }

    /**
     * Làm tròn VND; nếu total_amount trong DB lệch so với dòng + ship - giảm giá thì chỉnh DB và dùng số đối soát cho ZaloPay.
     */
    private long resolveAndSyncChargeAmountVnd(Order o) {
        double recomputed = computePayableFromOrderLines(o);
        Double stored = o.getTotalAmount();
        double charge = stored != null ? stored : recomputed;

        if (stored != null && Math.abs(stored - recomputed) > 1.0) {
            log.warn("Order {}: totalAmount={} differs from items+shipping-discount={}. Syncing DB for payment.",
                    o.getId(), stored, recomputed);
            o.setTotalAmount(recomputed);
            orderRepository.save(o);
            charge = recomputed;
        }

        long vnd = Math.round(charge);
        if (vnd <= 0) {
            throw new SystemException(SystemErrorCode.INVALID_PARAMETER);
        }
        return vnd;
    }

    private String getCurrentTimeString(String format) {
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT+7"));
        SimpleDateFormat fmt = new SimpleDateFormat(format);
        fmt.setCalendar(cal);
        return fmt.format(cal.getTimeInMillis());
    }
}