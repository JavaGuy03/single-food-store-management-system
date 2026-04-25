package com.fm.foodmanagementsystem.modules.auth_service.services.imps;

import com.fm.foodmanagementsystem.modules.auth_service.mappers.RoleMapper;
import com.fm.foodmanagementsystem.modules.auth_service.models.entities.Permission;
import com.fm.foodmanagementsystem.modules.auth_service.models.entities.Role;
import com.fm.foodmanagementsystem.modules.auth_service.models.repositories.PermissionRepository;
import com.fm.foodmanagementsystem.modules.auth_service.models.repositories.RoleRepository;
import com.fm.foodmanagementsystem.modules.auth_service.resources.requests.RoleRequest;
import com.fm.foodmanagementsystem.modules.auth_service.resources.responses.RoleResponse;
import com.fm.foodmanagementsystem.modules.auth_service.services.interfaces.IRoleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleService implements IRoleService {
    RoleRepository roleRepository;
    PermissionRepository permissionRepository;
    RoleMapper roleMapper;

    public RoleResponse create(RoleRequest request) {
        List<Permission> permissions = (request.permissions() != null)
                ? permissionRepository.findAllById(request.permissions())
                : List.of();

        Role role = new Role();
        role.setName(request.name());
        role.setDescription(request.description());
        role.setPermissions(new HashSet<>(permissions));

        role = roleRepository.save(role);
        return roleMapper.mapToResponse(role);
    }

    public List<RoleResponse> getAll() {
        return roleRepository.findAll().stream()
                .map(roleMapper::mapToResponse)
                .toList();
    }

    public void delete(String name) {
        roleRepository.deleteById(name);
    }
}
