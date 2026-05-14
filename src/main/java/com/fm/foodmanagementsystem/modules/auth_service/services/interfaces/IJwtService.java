package com.fm.foodmanagementsystem.modules.auth_service.services.interfaces;

import com.fm.foodmanagementsystem.modules.auth_service.models.dtos.TokenPair;
import com.fm.foodmanagementsystem.modules.auth_service.models.entities.User;
import com.nimbusds.jwt.SignedJWT;

import java.text.ParseException;
import java.util.Map;

public interface IJwtService {
    TokenPair generateTokenPair(User user, boolean rememberMe);
    SignedJWT verifyRefreshToken(String token);
    Map<String, Object> refreshToken(String token);
    void invalidatedToken(SignedJWT signedJWT) throws ParseException;
}