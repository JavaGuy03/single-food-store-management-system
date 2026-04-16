package com.fm.foodmanagementsystem.modules.auth_service.resources.responses;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record UserResponse(
        String email,

        String firstName,

        String lastName,

        String phone,

        LocalDate dob,

        int gender,

        List<String> roles
) {
}
