package com.fm.foodmanagementsystem.modules.auth_service.resources.requests;

import com.fm.foodmanagementsystem.modules.auth_service.validations.constraints.PhoneConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.util.List;

public record RegisterUserRequest(
        @NotNull(message = "NOT_NULL")
        @Email(message = "INVALID_EMAIL")
        String email,

        @NotNull(message = "NOT_NULL")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,16}$",
                message = "INVALID_PASSWORD")
        String password,

        @NotNull(message = "NOT_NULL")
        String firstName,

        @NotNull(message = "NOT_NULL")
        String lastName,

        @NotNull(message = "NOT_NULL")
        @PhoneConstraint(length = 10, message = "INVALID_PHONE")
        String phone,

        @NotNull(message = "NOT_NULL")
        LocalDate dob,

        @NotNull(message = "NOT_NULL")
        int gender
) {
}
