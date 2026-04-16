package com.fm.foodmanagementsystem.modules.auth_service.resources.requests;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailRequest(
        @NotBlank(message = "NOT_BLANK")
        @Email(message = "INVALID_EMAIL")
        String email
) {}