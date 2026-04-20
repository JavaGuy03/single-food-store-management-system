package com.fm.foodmanagementsystem.modules.auth_service.services.interfaces;

import com.fm.foodmanagementsystem.modules.auth_service.resources.requests.*;
import com.fm.foodmanagementsystem.modules.auth_service.resources.responses.TokenResponse;
import com.fm.foodmanagementsystem.modules.auth_service.resources.responses.UserResponse;
import com.nimbusds.jose.JOSEException;

import java.text.ParseException;
import java.util.Map;

public interface IAuthService {
    TokenResponse login(LoginRequest request);
    Map<String, Object> refreshToken(String refreshToken);
    void logout(String token);

    void registerPendingUser(RegisterUserRequest request, String roleName);
    UserResponse verifyAndCreateUser(VerifyOtpRequest request);

    void sendForgotPasswordOTP(String email);
    void resetPassword(NewPasswordRequest request);
}