package com.fm.foodmanagementsystem.modules.auth_service.resources.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ResendOtpRequest(
        @NotBlank(message = "Email không được để trống")
        @Email(message = "Định dạng email không hợp lệ")
        String email,

        @NotBlank(message = "Type không được để trống")
        @Pattern(regexp = "^(REGISTER|FORGOT_PASSWORD)$", message = "Type chỉ được là REGISTER hoặc FORGOT_PASSWORD")
        String type
) {
}