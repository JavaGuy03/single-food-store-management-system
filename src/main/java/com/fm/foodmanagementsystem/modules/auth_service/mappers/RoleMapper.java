package com.fm.foodmanagementsystem.modules.auth_service.mappers;

import com.fm.foodmanagementsystem.modules.auth_service.models.entities.Role;
import com.fm.foodmanagementsystem.modules.auth_service.resources.responses.PermissionResponse;
import com.fm.foodmanagementsystem.modules.auth_service.resources.responses.RoleResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class RoleMapper {
    public RoleResponse mapToResponse(Role role) {
        List<PermissionResponse> permissionResponses = (role.getPermissions() != null)
                ? role.getPermissions().stream()
                .map(p -> PermissionResponse.builder()
                        .name(p.getName())
                        .description(p.getDescription())
                        .build())
                .toList()
                : Collections.emptyList();

        return RoleResponse.builder()
                .name(role.getName())
                .description(role.getDescription())
                .permissions(permissionResponses)
                .build();
    }
}
