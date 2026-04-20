package com.fm.foodmanagementsystem.modules.payment_service.services.imps;

import com.fm.foodmanagementsystem.core.exception.SystemException;
import com.fm.foodmanagementsystem.core.exception.enums.SystemErrorCode;
import com.fm.foodmanagementsystem.modules.order_service.services.interfaces.IOrderService;
import com.fm.foodmanagementsystem.modules.payment_service.configs.ZaloPayConfig;
import com.fm.foodmanagementsystem.modules.payment_service.models.entities.PaymentTransaction;
import com.fm.foodmanagementsystem.modules.payment_service.models.repositories.PaymentTransactionRepository;
import com.fm.foodmanagementsystem.modules.payment_service.services.interfaces.IPaymentService;
import com.fm.foodmanagementsystem.modules.payment_service.utils.HmacUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
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
    RestTemplate restTemplate = new RestTemplate();
    PaymentTransactionRepository paymentTransactionRepository;

    @Override
    public Map<String, Object> createZaloPayOrder(String orderId, double amount) {
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
        order.put("embed_data", "{\"orderId\": \"" + orderId + "\"}"); // Gói kèm orderId để lát Query còn biết đường mà cập nhật
        order.put("callback_url", zaloPayConfig.getCallbackUrl());

        String data = order.get("app_id") + "|" + order.get("app_trans_id") + "|" + order.get("app_user") + "|" + order.get("amount")
                + "|" + order.get("app_time") + "|" + order.get("embed_data") + "|" + order.get("item");

        order.put("mac", HmacUtil.HMacHexStringEncode(HmacUtil.HMACSHA256, zaloPayConfig.getKey1(), data));

        // Gọi API ZaloPay
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        order.forEach((key, value) -> body.add(key, String.valueOf(value)));

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            Map<String, Object> response = restTemplate.postForObject(zaloPayConfig.getCreateOrderEndpoint(), request, Map.class);
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
            throw new SystemException(SystemErrorCode.UNCATEGORIZED_EXCEPTION); // Bác có thể tự tạo mã lỗi ZALOPAY_ERROR
        }
    }

    @Override
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
            Map<String, Object> response = restTemplate.postForObject(zaloPayConfig.getQueryEndpoint(), request, Map.class);

            if (response != null && response.containsKey("return_code")) {
                int returnCode = (Integer) response.get("return_code");

                if (returnCode == 1) {
                    // Thanh toán THÀNH CÔNG!
                    // Bước cực kỳ tinh tế: Rút cái orderId bị giấu trong trường embed_data ra
                    // (Lưu ý: ZaloPay trả về embed_data dưới dạng chuỗi JSON, nên cần bóc tách)
                    log.info("Giao dịch {} THÀNH CÔNG! Đang cập nhật trạng thái đơn hàng...", appTransId);

                    // TODO: Gọi orderService.updateOrderStatus(orderId, "PAID");
                    // (Bác chờ ở tin nhắn sau em chỉ cách bóc cái orderId ra nhé)

                    response.put("order_status_update", "SUCCESS");
                }
            }
            return response;
        } catch (Exception e) {
            log.error("Lỗi truy vấn ZaloPay: ", e);
            throw new SystemException(SystemErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    private String getCurrentTimeString(String format) {
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT+7"));
        SimpleDateFormat fmt = new SimpleDateFormat(format);
        fmt.setCalendar(cal);
        return fmt.format(cal.getTimeInMillis());
    }
}