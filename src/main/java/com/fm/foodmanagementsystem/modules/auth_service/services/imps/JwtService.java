package com.fm.foodmanagementsystem.modules.auth_service.services.imps;

import com.fm.foodmanagementsystem.core.exception.SystemException;
import com.fm.foodmanagementsystem.core.exception.enums.SystemErrorCode;
import com.fm.foodmanagementsystem.core.services.interfaces.IRedisCacheService;
import com.fm.foodmanagementsystem.modules.auth_service.models.entities.Role;
import com.fm.foodmanagementsystem.modules.auth_service.models.entities.User;
import com.fm.foodmanagementsystem.modules.auth_service.models.repositories.UserRepository;
import com.fm.foodmanagementsystem.modules.auth_service.services.interfaces.IJwtService;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JwtService implements IJwtService {

    IRedisCacheService redisCacheService;
    UserRepository userRepository;

    @NonFinal @Value("${jwt.secret}")
    protected String SECRET_KEY;

    @NonFinal @Value("${jwt.access-token-expiration}")
    protected int ACCESS_TOKEN_EXPIRATION;

    @NonFinal @Value("${jwt.refresh-token-expiration}")
    protected int REFRESH_TOKEN_EXPIRATION;

    // SỬA BỔ SUNG: Đọc thời gian sống của Refresh Token khi chọn Nhớ tôi (Từ file yaml)
    @NonFinal @Value("${jwt.refresh-token-remember-me-expiration}")
    protected int REFRESH_TOKEN_REMEMBER_ME_EXPIRATION;

    @Override
    public TokenPair generateTokenPair(User user, boolean rememberMe) {
        String accessToken = generateToken(user, ACCESS_TOKEN_EXPIRATION, "access");

        // SỬA BỔ SUNG: Chọn hạn sử dụng dài nếu rememberMe = true
        long refreshExpiration = rememberMe ? REFRESH_TOKEN_REMEMBER_ME_EXPIRATION : REFRESH_TOKEN_EXPIRATION;
        String refreshToken = generateToken(user, refreshExpiration, "refresh");

        return TokenPair.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    private String generateToken(User user, long expiration, String tokenType) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet.Builder body = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .jwtID(UUID.randomUUID().toString())
                .claim("token-type", tokenType)
                .claim("user-id", user.getId())
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(expiration, ChronoUnit.SECONDS).toEpochMilli()
                ));

        if ("access".equals(tokenType)) {
            body.claim("scope", buildScope(user));
        }

        Payload payload = new Payload(body.build().toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SECRET_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new SystemException(SystemErrorCode.UNAUTHENTICATED);
        }
    }

    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        if (!CollectionUtils.isEmpty(user.getRoles())) {
            for (Role role : user.getRoles()) {
                stringJoiner.add("ROLE_" + role.getName());
            }
        }
        return stringJoiner.toString();
    }

    @Override
    public SignedJWT verifyRefreshToken(String token) throws JOSEException, ParseException {
        if (token == null || token.trim().isEmpty()) {
            throw new SystemException(SystemErrorCode.UNAUTHENTICATED);
        }

        JWSVerifier verifier = new MACVerifier(SECRET_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);

        if (!signedJWT.verify(verifier)) {
            throw new SystemException(SystemErrorCode.UNAUTHENTICATED);
        }

        if (signedJWT.getJWTClaimsSet().getExpirationTime().before(new Date())) {
            throw new SystemException(SystemErrorCode.UNAUTHENTICATED);
        }

        String tokenType = signedJWT.getJWTClaimsSet().getStringClaim("token-type");
        if (!"refresh".equals(tokenType)) {
            throw new SystemException(SystemErrorCode.UNAUTHENTICATED);
        }

        String jwtId = signedJWT.getJWTClaimsSet().getJWTID();
        if (redisCacheService.hasKey("jwt:blacklist:" + jwtId)) {
            throw new SystemException(SystemErrorCode.UNAUTHENTICATED);
        }

        return signedJWT;
    }

    @Override
    public Map<String, Object> refreshToken(String token) throws ParseException, JOSEException {
        SignedJWT jwt = verifyRefreshToken(token);
        invalidatedToken(jwt);

        String email = jwt.getJWTClaimsSet().getSubject();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new SystemException(SystemErrorCode.USER_NOT_EXISTED));

        // SỬA BỔ SUNG: Tính toán thời gian sống của token cũ để biết user có đang dùng chế độ Remember Me hay không
        long issueTime = jwt.getJWTClaimsSet().getIssueTime().getTime();
        long expirationTime = jwt.getJWTClaimsSet().getExpirationTime().getTime();
        long durationInSeconds = (expirationTime - issueTime) / 1000;

        boolean isRememberMe = durationInSeconds > REFRESH_TOKEN_EXPIRATION;

        // Truyền trạng thái isRememberMe vào token mới
        TokenPair tokenPair = generateTokenPair(user, isRememberMe);

        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", tokenPair.getAccessToken());
        result.put("refreshToken", tokenPair.getRefreshToken());
        result.put("role", buildScope(user));

        return result;
    }

    @Override
    public void invalidatedToken(SignedJWT signedJWT) throws ParseException {
        String jwtId = signedJWT.getJWTClaimsSet().getJWTID();
        Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        long timeLeftBySeconds = (expirationTime.getTime() - System.currentTimeMillis()) / 1000;

        if (timeLeftBySeconds > 0) {
            redisCacheService.set("jwt:blacklist:" + jwtId, "revoked", timeLeftBySeconds, TimeUnit.SECONDS);
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class TokenPair {
        String accessToken;
        String refreshToken;
    }
}