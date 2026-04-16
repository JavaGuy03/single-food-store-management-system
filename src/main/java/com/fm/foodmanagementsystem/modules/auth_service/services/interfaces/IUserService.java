package com.fm.foodmanagementsystem.modules.auth_service.services.interfaces;

import com.fm.foodmanagementsystem.modules.auth_service.resources.requests.UserCreationRequest;
import com.fm.foodmanagementsystem.modules.auth_service.resources.requests.UserUpdateRequest;
import com.fm.foodmanagementsystem.modules.auth_service.resources.responses.UserResponse;
import org.springframework.data.domain.Page;

public interface IUserService {
    UserResponse createUser(UserCreationRequest request);
    UserResponse updateUserById(String id, UserUpdateRequest request);
    UserResponse getUserById(String id);
    UserResponse getMe();
    Page<UserResponse> getAllUsers(int page, int size);
    void deleteUserById(String id);
}
