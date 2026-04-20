package com.fm.foodmanagementsystem.modules.auth_service.filters;

import com.fm.foodmanagementsystem.core.services.interfaces.IRedisCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtRedisValidator implements OAuth2TokenValidator<Jwt> {

    private final IRedisCacheService redisCacheService;

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        String jwtId = jwt.getId();

        // Kiểm tra xem ID của token có nằm trong danh sách đen (Blacklist) của Redis không
        if (jwtId != null && redisCacheService.hasKey("jwt:blacklist:" + jwtId)) {
            // Nếu có trong blacklist, trả về lỗi. Spring Security sẽ chặn request lại với mã 401.
            OAuth2Error error = new OAuth2Error("invalid_token", "This token has been revoked", null);
            return OAuth2TokenValidatorResult.failure(error);
        }

        // Nếu không có trong blacklist, cho đi tiếp
        return OAuth2TokenValidatorResult.success();
    }
}
