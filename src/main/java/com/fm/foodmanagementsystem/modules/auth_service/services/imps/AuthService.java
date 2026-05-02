package com.fm.foodmanagementsystem.modules.auth_service.services.imps;

import com.fm.foodmanagementsystem.core.exception.SystemException;
import com.fm.foodmanagementsystem.core.exception.enums.SystemErrorCode;
import com.fm.foodmanagementsystem.core.services.interfaces.IEmailService;
import com.fm.foodmanagementsystem.core.services.interfaces.IRedisCacheService;
import com.fm.foodmanagementsystem.modules.auth_service.mappers.UserMapper;
import com.fm.foodmanagementsystem.modules.auth_service.models.dtos.PendingUserDto;
import com.fm.foodmanagementsystem.modules.auth_service.models.dtos.TokenPair;
import com.fm.foodmanagementsystem.modules.auth_service.models.entities.Role;
import com.fm.foodmanagementsystem.modules.auth_service.models.entities.User;
import com.fm.foodmanagementsystem.modules.auth_service.models.repositories.RoleRepository;
import com.fm.foodmanagementsystem.modules.auth_service.models.repositories.UserRepository;
import com.fm.foodmanagementsystem.modules.auth_service.resources.requests.*;
import com.fm.foodmanagementsystem.modules.auth_service.resources.responses.TokenResponse;
import com.fm.foodmanagementsystem.modules.auth_service.resources.responses.UserResponse;
import com.fm.foodmanagementsystem.modules.auth_service.services.interfaces.IAuthService;
import com.fm.foodmanagementsystem.modules.auth_service.services.interfaces.IJwtService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthService implements IAuthService {

    UserRepository userRepository;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;
    IJwtService jwtService;
    IRedisCacheService redisCacheService;
    UserMapper userMapper;
    IEmailService emailService;

    @Override
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new SystemException(SystemErrorCode.USER_NOT_EXISTED));

        // C3: Kiểm tra tài khoản có bị khóa không
        if (!user.getIsActive()) {
            throw new SystemException(SystemErrorCode.USER_DISABLED);
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new SystemException(SystemErrorCode.UNAUTHENTICATED);
        }

        // SỬA BỔ SUNG: Truyền cờ rememberMe từ request vào hàm sinh Token
        TokenPair tokenPair = jwtService.generateTokenPair(user, request.rememberMe());

        return TokenResponse.builder()
                .accessToken(tokenPair.getAccessToken())
                .refreshToken(tokenPair.getRefreshToken())
                .authenticated(true)
                .build();
    }

    @Override
    public Map<String, Object> refreshToken(String refreshToken) {
        return jwtService.refreshToken(refreshToken);
    }

    @Override
    public void logout(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            jwtService.invalidatedToken(signedJWT);
        } catch (ParseException e) {
            log.error("Lỗi khi parse token lúc logout: {}", e.getMessage());
            throw new SystemException(SystemErrorCode.UNAUTHENTICATED);
        } catch (Exception e) {
            log.error("Lỗi hệ thống khi logout: {}", e.getMessage());
            throw new SystemException(SystemErrorCode.UNAUTHENTICATED);
        }
    }

    @Override
    public void registerPendingUser(RegisterUserRequest request, String roleName) {
        if (userRepository.existsByEmail(request.email())) {
            throw new SystemException(SystemErrorCode.USER_EXISTED);
        }

        PendingUserDto pendingUser = PendingUserDto.builder()
                .email(request.email())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .password(passwordEncoder.encode(request.password()))
                .phone(request.phone())
                .dob(request.dob())
                .gender(request.gender())
                .roles(java.util.List.of(roleName))
                .build();

        redisCacheService.set("pending_user:" + request.email(), pendingUser, 15, TimeUnit.MINUTES);

        generateAndSendOtp(request.email(), "REGISTER");
    }

    @Override
    @Transactional
    public UserResponse verifyAndCreateUser(VerifyOtpRequest request) {
        verifyOtp(request.email(), request.otpCode(), "REGISTER");

        PendingUserDto pendingUser = redisCacheService.get("pending_user:" + request.email(), PendingUserDto.class);
        if (pendingUser == null) {
            throw new SystemException(SystemErrorCode.USER_NOT_EXISTED);
        }

        User user = User.builder()
                .email(pendingUser.getEmail())
                .password(pendingUser.getPassword())
                .firstName(pendingUser.getFirstName())
                .lastName(pendingUser.getLastName())
                .phone(pendingUser.getPhone())
                .dob(pendingUser.getDob())
                .gender(pendingUser.getGender())
                .build();

        if (pendingUser.getRoles() != null && !pendingUser.getRoles().isEmpty()) {
            List<Role> roles = roleRepository.findAllById(pendingUser.getRoles());
            if (roles.size() != pendingUser.getRoles().size()) {
                throw new SystemException(SystemErrorCode.DATA_NOT_FOUND);
            }
            user.setRoles(new HashSet<>(roles));
        }

        User savedUser = userRepository.save(user);

        redisCacheService.delete("pending_user:" + request.email());
        redisCacheService.delete("otp:REGISTER:" + request.email());

        return userMapper.mapToUserResponse(savedUser);
    }

    @Override
    public void sendForgotPasswordOTP(String email) {
        if (!userRepository.existsByEmail(email)) {
            throw new SystemException(SystemErrorCode.USER_NOT_EXISTED);
        }
        generateAndSendOtp(email, "FORGOT_PASSWORD");
    }

    @Override
    @Transactional
    public void resetPassword(NewPasswordRequest request) {
        verifyOtp(request.email(), request.otpCode(), "FORGOT_PASSWORD");

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new SystemException(SystemErrorCode.USER_NOT_EXISTED));

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        redisCacheService.delete("otp:FORGOT_PASSWORD:" + request.email());
    }

    @Override
    public void resendOtp(String email, String type) {
        // Kiểm tra loại OTP để xác định luồng
        if ("REGISTER".equals(type)) {
            // Nếu là gửi lại mã đăng ký, phải kiểm tra xem user này có đang "chờ" trong Redis không
            PendingUserDto pendingUser = redisCacheService.get("pending_user:" + email, PendingUserDto.class);
            if (pendingUser == null) {
                // Nếu không có trong Redis, tức là đã quá 15 phút hoặc chưa từng đăng ký
                throw new SystemException(SystemErrorCode.USER_NOT_EXISTED);
            }
        } else if ("FORGOT_PASSWORD".equals(type)) {
            // Nếu là gửi lại mã quên mật khẩu, kiểm tra xem user có thật trong DB không
            if (!userRepository.existsByEmail(email)) {
                throw new SystemException(SystemErrorCode.USER_NOT_EXISTED);
            }
        } else {
            throw new SystemException(SystemErrorCode.INVALID_PARAMETER); // Nếu FE truyền type tào lao
        }

        // Gọi lại hàm tiện ích để sinh mã mới đè lên mã cũ trong Redis (hạn 5 phút mới) và gửi mail
        generateAndSendOtp(email, type);
    }

    private void generateAndSendOtp(String email, String type) {
        String otp = String.format("%06d", new SecureRandom().nextInt(1_000_000));
        String redisKey = "otp:" + type + ":" + email;

        redisCacheService.set(redisKey, otp, 5, TimeUnit.MINUTES);
        // NOTE: OTP is intentionally NOT logged here for security. Use email delivery only.

        // 👇 GỌI EMAIL SERVICE ĐỂ GỬI MAIL THẬT 👇
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("otpCode", otp);
            templateModel.put("email", email);

            String subject;
            String templateName;

            if ("REGISTER".equals(type)) {
                subject = "Mã xác nhận đăng ký tài khoản FastBite";
                templateName = "register-otp"; // Tên file HTML
            } else {
                subject = "Mã xác nhận khôi phục mật khẩu FastBite";
                templateName = "forgot-password-otp"; // Tên file HTML
            }

            // Gọi hàm gửi mail chạy ngầm
            emailService.sendHtmlEmail(email, subject, templateName, templateModel);

        } catch (Exception e) {
            log.error("Lỗi khi gửi email OTP: {}", e.getMessage());
            // Có thể throw Exception nếu bắt buộc phải gửi được mail mới cho chạy tiếp
        }
    }

    private void verifyOtp(String email, String otpCode, String type) {
        String redisKey = "otp:" + type + ":" + email;
        String cachedOtp = redisCacheService.get(redisKey, String.class);

        if (cachedOtp == null || !cachedOtp.equals(otpCode)) {
            throw new SystemException(SystemErrorCode.UNAUTHENTICATED);
        }
    }
}