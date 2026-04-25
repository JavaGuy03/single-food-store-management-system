package com.fm.foodmanagementsystem.modules.auth_service.resources.requests;

import com.fm.foodmanagementsystem.modules.auth_service.validations.constraints.PhoneConstraint;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record UserUpdateRequest(
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,16}$",
                message = "INVALID_PASSWORD")
        String password,

        String firstName,

        String lastName,

        @PhoneConstraint(length = 10, message = "INVALID_PHONE")
        String phone,

        LocalDate dob,

        Integer gender // Đổi từ int → Integer để phân biệt null (không gửi) vs 0 (giá trị thật)
) {
}
