package com.fm.foodmanagementsystem.modules.auth_service.services.interfaces;

import com.fm.foodmanagementsystem.modules.auth_service.resources.requests.UserCreationRequest;
import com.fm.foodmanagementsystem.modules.auth_service.resources.requests.UserUpdateRequest;
import com.fm.foodmanagementsystem.modules.auth_service.resources.responses.UserResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IUserService {
    UserResponse createUser(UserCreationRequest request);
    UserResponse updateUserById(String id, UserUpdateRequest request);

    // API dành riêng cho Admin để đổi quyền
    UserResponse updateUserRoles(String id, List<String> roleNames);

    UserResponse getUserById(String id);

    // Đã gộp hàm search và filter role vào đây
    Page<UserResponse> getAllUsers(String search, String role, int page, int size);

    void deleteUserById(String id);

    void unlockUserById(String id);
}