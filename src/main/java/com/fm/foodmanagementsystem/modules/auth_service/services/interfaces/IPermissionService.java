package com.fm.foodmanagementsystem.modules.auth_service.services.interfaces;

import com.fm.foodmanagementsystem.modules.auth_service.resources.requests.PermissionRequest;
import com.fm.foodmanagementsystem.modules.auth_service.resources.responses.PermissionResponse;

import java.util.List;

public interface IPermissionService {
    PermissionResponse create(PermissionRequest request);
    List<PermissionResponse> getAll();
    void delete(String name);
}
