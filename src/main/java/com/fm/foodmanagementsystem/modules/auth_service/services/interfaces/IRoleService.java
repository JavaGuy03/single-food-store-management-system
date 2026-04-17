package com.fm.foodmanagementsystem.modules.auth_service.services.interfaces;

import com.fm.foodmanagementsystem.modules.auth_service.resources.requests.RoleRequest;
import com.fm.foodmanagementsystem.modules.auth_service.resources.responses.RoleResponse;

import java.util.List;

public interface IRoleService {
    RoleResponse create(RoleRequest request);
    List<RoleResponse> getAll();
    void delete(String name);
}
