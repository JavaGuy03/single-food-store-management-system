package com.fm.foodmanagementsystem.modules.auth_service.resources.requests;
import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(
        @NotBlank(message = "NOT_BLANK")
        String token
) {}