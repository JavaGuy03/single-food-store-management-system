package com.fm.foodmanagementsystem.modules.auth_service.controllers;

import com.fm.foodmanagementsystem.core.exception.SystemException;
import com.fm.foodmanagementsystem.core.exception.enums.SystemErrorCode;
import com.fm.foodmanagementsystem.core.response.ApiResponse;
import com.fm.foodmanagementsystem.modules.auth_service.resources.requests.UserCreationRequest;
import com.fm.foodmanagementsystem.modules.auth_service.resources.requests.UserUpdateRequest;
import com.fm.foodmanagementsystem.modules.auth_service.resources.responses.UserResponse;
import com.fm.foodmanagementsystem.modules.auth_service.services.interfaces.IUserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    IUserService userService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request) {
        return ApiResponse.<UserResponse>builder()
                .message("Tạo người dùng thành công")
                .result(userService.createUser(request))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UserResponse> updateUser(
            @PathVariable String id,
            @RequestBody @Valid UserUpdateRequest request) {
        // M-5: Ownership check — a user can only update their own profile
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String callerId = jwt.getClaimAsString("user-id");
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !callerId.equals(id)) {
            throw new SystemException(SystemErrorCode.UNAUTHORIZED_ACTION);
        }
        return ApiResponse.<UserResponse>builder()
                .message("Cập nhật thông tin thành công")
                .result(userService.updateUserById(id, request))
                .build();
    }

    // Cấp riêng API này cho Admin sửa quyền
    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> updateUserRoles(
            @PathVariable String id,
            @RequestBody List<String> roleNames) {
        return ApiResponse.<UserResponse>builder()
                .message("Cập nhật phân quyền thành công")
                .result(userService.updateUserRoles(id, roleNames))
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> getUserById(@PathVariable String id) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getUserById(id))
                .build();
    }

    @GetMapping("/my-info")
    public ApiResponse<UserResponse> getMyInfo() {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getMe())
                .build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<UserResponse>> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<Page<UserResponse>>builder()
                .result(userService.getAllUsers(search, role, page, size))
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> deleteUser(@PathVariable String id) {
        userService.deleteUserById(id);
        return ApiResponse.<String>builder()
                .message("Đã khoá tài khoản người dùng thành công")
                .build();
    }
}