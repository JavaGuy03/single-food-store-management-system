package com.fm.foodmanagementsystem.modules.auth_service.services.interfaces;

import com.fm.foodmanagementsystem.modules.auth_service.models.entities.User;
import com.fm.foodmanagementsystem.modules.auth_service.services.imps.JwtService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;

import java.text.ParseException;
import java.util.Map;

public interface IJwtService {
    JwtService.TokenPair generateTokenPair(User user, boolean rememberMe);
    SignedJWT verifyRefreshToken(String token) throws JOSEException, ParseException;
    Map<String, Object> refreshToken(String token) throws ParseException, JOSEException;
    void invalidatedToken(SignedJWT signedJWT) throws ParseException;
}