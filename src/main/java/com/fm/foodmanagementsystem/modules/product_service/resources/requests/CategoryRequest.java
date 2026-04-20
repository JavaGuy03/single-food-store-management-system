package com.fm.foodmanagementsystem.modules.product_service.resources.requests;

import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

public record CategoryRequest(
        @NotBlank(message = "NOT_BLANK")
        String name,

        String description,

        MultipartFile file
) {}