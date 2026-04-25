package com.fm.foodmanagementsystem.modules.setting_service.controllers;

import com.fm.foodmanagementsystem.core.response.ApiResponse;
import com.fm.foodmanagementsystem.modules.setting_service.resources.requests.BannerRequest;
import com.fm.foodmanagementsystem.modules.setting_service.resources.requests.StoreSettingRequest;
import com.fm.foodmanagementsystem.modules.setting_service.resources.responses.BannerResponse;
import com.fm.foodmanagementsystem.modules.setting_service.resources.responses.StoreSettingResponse;
import com.fm.foodmanagementsystem.modules.setting_service.services.interfaces.ISettingService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SettingController {

    ISettingService settingService;

    // --- Store Settings ---
    @GetMapping("/store")
    public ApiResponse<StoreSettingResponse> getStoreSetting() {
        return ApiResponse.<StoreSettingResponse>builder().result(settingService.getStoreSetting()).build();
    }

    @PutMapping("/store")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<StoreSettingResponse> updateStoreSetting(@Valid @RequestBody StoreSettingRequest request) {
        return ApiResponse.<StoreSettingResponse>builder().result(settingService.updateStoreSetting(request)).build();
    }

    // --- Banners ---
    @GetMapping("/banners")
    public ApiResponse<List<BannerResponse>> getActiveBanners() {
        return ApiResponse.<List<BannerResponse>>builder().result(settingService.getAllBanners(true)).build();
    }

    @GetMapping("/banners/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<BannerResponse>> getAllBanners() {
        return ApiResponse.<List<BannerResponse>>builder().result(settingService.getAllBanners(false)).build();
    }

    @PostMapping(value = "/banners", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<BannerResponse> createBanner(@Valid @ModelAttribute BannerRequest request) {
        return ApiResponse.<BannerResponse>builder().result(settingService.createBanner(request)).build();
    }

    @PutMapping(value = "/banners/{id}", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<BannerResponse> updateBanner(@PathVariable Long id, @Valid @ModelAttribute BannerRequest request) {
        return ApiResponse.<BannerResponse>builder().result(settingService.updateBanner(id, request)).build();
    }

    @DeleteMapping("/banners/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteBanner(@PathVariable Long id) {
        settingService.deleteBanner(id);
        return ApiResponse.<Void>builder().build();
    }

    @PatchMapping("/banners/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> changeBannerStatus(@PathVariable Long id, @RequestParam boolean isActive) {
        settingService.changeBannerStatus(id, isActive);
        return ApiResponse.<Void>builder().build();
    }
}
