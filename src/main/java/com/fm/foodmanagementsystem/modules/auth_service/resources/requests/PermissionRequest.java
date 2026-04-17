package com.fm.foodmanagementsystem.modules.auth_service.resources.requests;
import jakarta.validation.constraints.NotBlank;

public record PermissionRequest(
        @NotBlank(message = "NOT_BLANK")
        String name,

        String description
) {}
