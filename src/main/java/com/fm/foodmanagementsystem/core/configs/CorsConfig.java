package com.fm.foodmanagementsystem.core.configs;

// CORS configuration is now handled in SecurityConfig.corsConfigurationSource()
// This class is intentionally left empty to avoid duplicate CORS configs.
// Can be safely deleted if not needed.

import org.springframework.context.annotation.Configuration;

@Configuration
public class CorsConfig {
    // Removed duplicate WebMvcConfigurer CORS mapping.
    // CORS is configured in SecurityConfig via CorsConfigurationSource bean.
}