package com.fm.foodmanagementsystem.modules.auth_service.controllers;

import com.fm.foodmanagementsystem.core.response.ApiResponse;
import com.fm.foodmanagementsystem.modules.auth_service.resources.requests.*;
import com.fm.foodmanagementsystem.modules.auth_service.resources.responses.TokenResponse;
import com.fm.foodmanagementsystem.modules.auth_service.resources.responses.UserResponse;
import com.fm.foodmanagementsystem.modules.auth_service.services.interfaces.IAuthService;
import com.nimbusds.jose.JOSEException;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {

    IAuthService authService;

    // =====================================================================
    // 1. ĐĂNG NHẬP & QUẢN LÝ TOKEN
    // =====================================================================

    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@RequestBody @Valid LoginRequest request) {
        return ApiResponse.<TokenResponse>builder()
                .message("Login successfully")
                .result(authService.login(request))
                .build();
    }

    @PostMapping("/refresh-token")
    public ApiResponse<Map<String, Object>> refreshToken(@RequestBody @Valid RefreshTokenRequest request) {
        return ApiResponse.<Map<String, Object>>builder()
                .message("Refresh token successfully")
                .result(authService.refreshToken(request.refreshToken()))
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<String> logout(@RequestBody @Valid LogoutRequest request) {

        authService.logout(request.token());

        return ApiResponse.<String>builder()
                .message("Logout successfully")
                // Không truyền result, Jackson sẽ tự động giấu field result đi
                .build();
    }

    // =====================================================================
    // 2. ĐĂNG KÝ (LUỒNG PENDING -> OTP -> ACTIVE)
    // =====================================================================

    @PostMapping("/register-admin")
    public ApiResponse<String> registerAdmin(@RequestBody @Valid UserCreationRequest request) {
        authService.registerPendingUser(request, "ADMIN");

        return ApiResponse.<String>builder()
                .message("Please check your email for the OTP code")
                .build();
    }

    @PostMapping("/register-customer")
    public ApiResponse<String> registerCustomer(@RequestBody @Valid UserCreationRequest request) {
        authService.registerPendingUser(request, "USER");

        return ApiResponse.<String>builder()
                .message("Please check your email for the OTP code")
                .build();
    }

    @PostMapping("/verify-register")
    public ApiResponse<UserResponse> verifyRegister(@RequestBody @Valid VerifyOtpRequest request) {
        return ApiResponse.<UserResponse>builder()
                .message("User registered successfully")
                .result(authService.verifyAndCreateUser(request))
                .build();
    }

    // =====================================================================
    // 3. QUÊN MẬT KHẨU
    // =====================================================================

    @PostMapping("/forgot-password")
    public ApiResponse<String> forgotPassword(@RequestBody @Valid EmailRequest request) {
        authService.sendForgotPasswordOTP(request.email());

        return ApiResponse.<String>builder()
                .message("OTP sent to your email")
                .build();
    }

    @PostMapping("/reset-password")
    public ApiResponse<String> resetPassword(@RequestBody @Valid NewPasswordRequest request) {
        authService.resetPassword(request);

        return ApiResponse.<String>builder()
                .message("Password reset successfully")
                .build();
    }
}