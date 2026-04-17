package com.fm.foodmanagementsystem.modules.auth_service.resources.responses;
import lombok.Builder;

@Builder
public record PermissionResponse(
        String name,
        String description
) {}