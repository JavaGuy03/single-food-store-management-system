package com.fm.foodmanagementsystem.modules.setting_service.services.interfaces;

import com.fm.foodmanagementsystem.modules.setting_service.resources.requests.BannerRequest;
import com.fm.foodmanagementsystem.modules.setting_service.resources.requests.StoreSettingRequest;
import com.fm.foodmanagementsystem.modules.setting_service.resources.responses.BannerResponse;
import com.fm.foodmanagementsystem.modules.setting_service.resources.responses.StoreSettingResponse;

import java.util.List;

public interface ISettingService {
    StoreSettingResponse getStoreSetting();
    StoreSettingResponse updateStoreSetting(StoreSettingRequest request);
    
    List<BannerResponse> getAllBanners(boolean activeOnly);
    BannerResponse createBanner(BannerRequest request);
    BannerResponse updateBanner(Long id, BannerRequest request);
    void deleteBanner(Long id);
    void changeBannerStatus(Long id, boolean isActive);
}
