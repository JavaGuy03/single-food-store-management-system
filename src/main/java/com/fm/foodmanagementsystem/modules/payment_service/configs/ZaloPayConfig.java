package com.fm.foodmanagementsystem.modules.payment_service.configs;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ZaloPayConfig {

    @Value("${zalo-pay.app-id}")
    String appId;

    @Value("${zalo-pay.key1}")
    String key1;

    @Value("${zalo-pay.key2}")
    String key2;

    @Value("${zalo-pay.create-order-url}")
    String createOrderEndpoint;

    @Value("${zalo-pay.order-status-url}")
    String queryEndpoint;

    @Value("${zalo-pay.callback-url}")
    String callbackUrl;
}
