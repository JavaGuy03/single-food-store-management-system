package com.fm.foodmanagementsystem.modules.setting_service.resources.requests;

import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

public record BannerRequest(
        @NotBlank(message = "NOT_BLANK")
        String title,
        
        MultipartFile file,
        
        String linkUrl,
        
        Boolean isActive,
        
        Integer displayOrder
) {
}
