package com.fm.foodmanagementsystem.modules.auth_service.resources.requests;

import com.fm.foodmanagementsystem.modules.auth_service.validations.constraints.PhoneConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.util.List;

public record UserUpdateRequest(
        @Email(message = "INVALID_EMAIL")
        String email,

        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,16}$",
                message = "INVALID_PASSWORD")
        String password,

        String firstName,

        String lastName,

        @PhoneConstraint(length = 10, message = "INVALID_PHONE")
        String phone,

        LocalDate dob,

        int gender,

        List<String> roles
) {
}
