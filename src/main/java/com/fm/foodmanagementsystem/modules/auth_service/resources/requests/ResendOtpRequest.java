package com.fm.foodmanagementsystem.modules.auth_service.resources.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ResendOtpRequest(
        @NotBlank(message = "NOT_BLANK")
        @Email(message = "INVALID_EMAIL")
        String email,

        @NotBlank(message = "NOT_BLANK")
        @Pattern(regexp = "^(REGISTER|FORGOT_PASSWORD)$", message = "INVALID_PARAMETER")
        String type
) {
}