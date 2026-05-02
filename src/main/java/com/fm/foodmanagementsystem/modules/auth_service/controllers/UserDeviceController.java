package com.fm.foodmanagementsystem.modules.auth_service.controllers;

import com.fm.foodmanagementsystem.core.response.ApiResponse;
import com.fm.foodmanagementsystem.modules.auth_service.resources.requests.FcmTokenRequest;
import com.fm.foodmanagementsystem.modules.auth_service.services.interfaces.IUserDeviceService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user/devices")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserDeviceController {

    IUserDeviceService deviceService;

    @PostMapping("/register")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> registerDevice(@Valid @RequestBody FcmTokenRequest request) {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = jwt.getClaimAsString("user-id");
        deviceService.registerDevice(userId, request);
        return ApiResponse.<Void>builder().build();
    }

    @DeleteMapping("/unregister")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> unregisterDevice(@RequestParam String fcmToken) {
        deviceService.unregisterDevice(fcmToken);
        return ApiResponse.<Void>builder().build();
    }
}
