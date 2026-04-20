package com.fm.foodmanagementsystem.modules.product_service.resources.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record FoodRequest(
        @NotBlank(message = "NOT_BLANK")
        String name,

        String description,

        @NotNull(message = "NOT_NULL")
        @Min(value = 0, message = "INVALID_MIN")
        Double price,

        @NotNull(message = "NOT_NULL")
        Long categoryId,

        MultipartFile file
) {}