package com.fm.foodmanagementsystem.modules.auth_service.controllers;

import com.fm.foodmanagementsystem.core.response.ApiResponse;
import com.fm.foodmanagementsystem.modules.auth_service.resources.requests.PermissionRequest;
import com.fm.foodmanagementsystem.modules.auth_service.resources.responses.PermissionResponse;
import com.fm.foodmanagementsystem.modules.auth_service.services.interfaces.IPermissionService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionController {

    IPermissionService permissionService;

    @PostMapping
    public ApiResponse<PermissionResponse> create(@RequestBody @Valid PermissionRequest request) {
        return ApiResponse.<PermissionResponse>builder()
                .message("Permission created successfully")
                .result(permissionService.create(request))
                .build();
    }

    @GetMapping
    public ApiResponse<List<PermissionResponse>> getAll() {
        return ApiResponse.<List<PermissionResponse>>builder()
                .result(permissionService.getAll())
                .build();
    }

    @DeleteMapping("/{name}")
    public ApiResponse<String> delete(@PathVariable String name) {
        permissionService.delete(name);
        return ApiResponse.<String>builder()
                .message("Permission deleted successfully")
                .build();
    }
}