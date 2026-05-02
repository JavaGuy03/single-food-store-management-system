package com.fm.foodmanagementsystem.modules.auth_service.controllers;

import com.fm.foodmanagementsystem.core.response.ApiResponse;
import com.fm.foodmanagementsystem.modules.auth_service.resources.requests.UserAddressRequest;
import com.fm.foodmanagementsystem.modules.auth_service.resources.responses.UserAddressResponse;
import com.fm.foodmanagementsystem.modules.auth_service.services.interfaces.IUserAddressService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user/addresses")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserAddressController {

    IUserAddressService addressService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<UserAddressResponse>> getMyAddresses() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = jwt.getClaimAsString("user-id");
        return ApiResponse.<List<UserAddressResponse>>builder().result(addressService.getMyAddresses(userId)).build();
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UserAddressResponse> addAddress(@Valid @RequestBody UserAddressRequest request) {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = jwt.getClaimAsString("user-id");
        return ApiResponse.<UserAddressResponse>builder().result(addressService.addAddress(userId, request)).build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UserAddressResponse> updateAddress(@PathVariable Long id, @Valid @RequestBody UserAddressRequest request) {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = jwt.getClaimAsString("user-id");
        return ApiResponse.<UserAddressResponse>builder().result(addressService.updateAddress(userId, id, request)).build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> deleteAddress(@PathVariable Long id) {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = jwt.getClaimAsString("user-id");
        addressService.deleteAddress(userId, id);
        return ApiResponse.<Void>builder().build();
    }

    @PatchMapping("/{id}/default")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> setDefaultAddress(@PathVariable Long id) {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = jwt.getClaimAsString("user-id");
        addressService.setDefaultAddress(userId, id);
        return ApiResponse.<Void>builder().build();
    }
}
