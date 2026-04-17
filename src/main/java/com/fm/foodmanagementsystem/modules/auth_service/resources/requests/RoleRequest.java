package com.fm.foodmanagementsystem.modules.auth_service.resources.requests;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record RoleRequest(
        @NotBlank(message = "NOT_BLANK") String name,
        String description,

        List<String> permissions // Danh sách tên các quyền (VD: ["CREATE_USER", "DELETE_POST"])
) {}