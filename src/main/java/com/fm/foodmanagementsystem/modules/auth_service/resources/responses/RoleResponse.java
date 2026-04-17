package com.fm.foodmanagementsystem.modules.auth_service.resources.responses;
import lombok.Builder;
import java.util.List;

@Builder
public record RoleResponse(
        String name,
        String description,
        List<PermissionResponse> permissions
) {}