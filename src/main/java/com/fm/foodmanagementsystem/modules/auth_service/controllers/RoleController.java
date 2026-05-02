package com.fm.foodmanagementsystem.modules.auth_service.controllers;

import com.fm.foodmanagementsystem.core.response.ApiResponse;
import com.fm.foodmanagementsystem.modules.auth_service.resources.requests.RoleRequest;
import com.fm.foodmanagementsystem.modules.auth_service.resources.responses.RoleResponse;
import com.fm.foodmanagementsystem.modules.auth_service.services.interfaces.IRoleService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleController {

    IRoleService roleService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<RoleResponse> create(@RequestBody @Valid RoleRequest request) {
        return ApiResponse.<RoleResponse>builder()
                .message("Role created successfully")
                .result(roleService.create(request))
                .build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<RoleResponse>> getAll() {
        return ApiResponse.<List<RoleResponse>>builder()
                .result(roleService.getAll())
                .build();
    }

    @DeleteMapping("/{name}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> delete(@PathVariable String name) {
        roleService.delete(name);
        return ApiResponse.<String>builder()
                .message("Role deleted successfully")
                .build();
    }
}