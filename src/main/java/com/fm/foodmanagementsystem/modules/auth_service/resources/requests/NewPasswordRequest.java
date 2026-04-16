package com.fm.foodmanagementsystem.modules.auth_service.resources.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record NewPasswordRequest(
        @NotBlank(message = "NOT_BLANK")
        @Email(message = "INVALID_EMAIL")
        String email,

        @NotBlank(message = "NOT_BLANK")
        @Size(min = 6, max = 6, message = "INVALID_OTP")
        String otpCode,

        @NotBlank(message = "NOT_BLANK")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,16}$",
                message = "INVALID_PASSWORD")
        String newPassword
) {
}