package com.fm.foodmanagementsystem.core.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Áp dụng cho toàn bộ API (tất cả các endpoint)
                .allowedOriginPatterns("*") // Cho phép mọi Domain (Web, Mobile, Localhost) gọi vào
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS") // Cho phép mọi phương thức
                .allowedHeaders("*") // Cho phép mọi loại Header (đặc biệt là Authorization chứa token)
                .allowCredentials(true) // Cho phép gửi Cookie/Token chéo domain
                .maxAge(3600); // Lưu cache cấu hình CORS trong 1 giờ để giảm tải request OPTIONS
    }
}