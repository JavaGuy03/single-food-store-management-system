package com.fm.foodmanagementsystem.modules.auth_service.services.interfaces;

import com.fm.foodmanagementsystem.modules.auth_service.resources.requests.LoginRequest;
import com.fm.foodmanagementsystem.modules.auth_service.resources.requests.NewPasswordRequest;
import com.fm.foodmanagementsystem.modules.auth_service.resources.requests.UserCreationRequest;
import com.fm.foodmanagementsystem.modules.auth_service.resources.requests.VerifyOtpRequest;
import com.fm.foodmanagementsystem.modules.auth_service.resources.responses.TokenResponse;
import com.fm.foodmanagementsystem.modules.auth_service.resources.responses.UserResponse;
import com.nimbusds.jose.JOSEException;

import java.text.ParseException;
import java.util.Map;

public interface IAuthService {
    TokenResponse login(LoginRequest request);
    Map<String, Object> refreshToken(String refreshToken) throws ParseException, JOSEException;
    void logout(String token) throws ParseException, JOSEException;

    void registerPendingUser(UserCreationRequest request);
    UserResponse verifyAndCreateUser(VerifyOtpRequest request);

    void sendForgotPasswordOTP(String email);
    void resetPassword(NewPasswordRequest request);
}