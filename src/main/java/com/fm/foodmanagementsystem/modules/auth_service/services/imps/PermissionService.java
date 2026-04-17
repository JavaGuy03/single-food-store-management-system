package com.fm.foodmanagementsystem.modules.auth_service.services.imps;

import com.fm.foodmanagementsystem.modules.auth_service.models.entities.Permission;
import com.fm.foodmanagementsystem.modules.auth_service.models.repositories.PermissionRepository;
import com.fm.foodmanagementsystem.modules.auth_service.resources.requests.PermissionRequest;
import com.fm.foodmanagementsystem.modules.auth_service.resources.responses.PermissionResponse;
import com.fm.foodmanagementsystem.modules.auth_service.services.interfaces.IPermissionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionService implements IPermissionService {
    PermissionRepository permissionRepository;

    @Override
    public PermissionResponse create(PermissionRequest request) {
        Permission permission = new Permission(request.name(), request.description());
        permission = permissionRepository.save(permission);
        return PermissionResponse.builder()
                .name(permission.getName())
                .description(permission.getDescription())
                .build();
    }

    @Override
    public List<PermissionResponse> getAll() {
        return permissionRepository.findAll().stream()
                .map(p -> PermissionResponse.builder()
                        .name(p.getName())
                        .description(p.getDescription())
                        .build())
                .toList();
    }

    @Override
    public void delete(String name) {
        permissionRepository.deleteById(name);
    }
}