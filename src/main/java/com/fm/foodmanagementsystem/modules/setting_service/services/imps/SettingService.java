package com.fm.foodmanagementsystem.modules.setting_service.services.imps;

import com.fm.foodmanagementsystem.core.exception.SystemException;
import com.fm.foodmanagementsystem.core.exception.enums.SystemErrorCode;
import com.fm.foodmanagementsystem.core.services.interfaces.IFileService;
import com.fm.foodmanagementsystem.modules.setting_service.models.entities.Banner;
import com.fm.foodmanagementsystem.modules.setting_service.models.entities.StoreSetting;
import com.fm.foodmanagementsystem.modules.setting_service.models.repositories.BannerRepository;
import com.fm.foodmanagementsystem.modules.setting_service.models.repositories.StoreSettingRepository;
import com.fm.foodmanagementsystem.modules.setting_service.resources.requests.BannerRequest;
import com.fm.foodmanagementsystem.modules.setting_service.resources.requests.StoreSettingRequest;
import com.fm.foodmanagementsystem.modules.setting_service.resources.responses.BannerResponse;
import com.fm.foodmanagementsystem.modules.setting_service.resources.responses.StoreSettingResponse;
import com.fm.foodmanagementsystem.modules.setting_service.services.interfaces.ISettingService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SettingService implements ISettingService {

    final StoreSettingRepository storeSettingRepository;
    final BannerRepository bannerRepository;
    final IFileService fileService;

    @lombok.experimental.NonFinal
    @Value("${openapi.service.url:http://localhost:8080}")
    String serverUrl;

    @Override
    public StoreSettingResponse getStoreSetting() {
        StoreSetting setting = storeSettingRepository.findAll().stream().findFirst()
                .orElseGet(() -> {
                    StoreSetting defaultSetting = StoreSetting.builder()
                            .storeName("Food Management System")
                            .hotline("1900 1234")
                            .isOpen(true)
                            .baseShippingFee(15000.0)
                            .freeShipThreshold(300000.0)
                            .build();
                    return storeSettingRepository.save(defaultSetting);
                });
        return mapToStoreSettingResponse(setting);
    }

    @Override
    @Transactional
    public StoreSettingResponse updateStoreSetting(StoreSettingRequest request) {
        StoreSetting setting = storeSettingRepository.findAll().stream().findFirst()
                .orElse(new StoreSetting());

        setting.setStoreName(request.storeName());
        setting.setHotline(request.hotline());
        setting.setIsOpen(request.isOpen());
        setting.setBaseShippingFee(request.baseShippingFee());
        setting.setFreeShipThreshold(request.freeShipThreshold());

        return mapToStoreSettingResponse(storeSettingRepository.save(setting));
    }

    @Override
    public List<BannerResponse> getAllBanners(boolean activeOnly) {
        List<Banner> banners = activeOnly ? bannerRepository.findAllByIsActiveTrueOrderByDisplayOrderAsc() : bannerRepository.findAll();
        return banners.stream().map(this::mapToBannerResponse).toList();
    }

    @Override
    @Transactional
    public BannerResponse createBanner(BannerRequest request) {
        if (request.file() == null || request.file().isEmpty()) {
            throw new SystemException(SystemErrorCode.INVALID_PARAMETER); // C-3 FIX: 400 not 500
        }

        String imageName = fileService.uploadFile(request.file());
        
        Banner banner = Banner.builder()
                .title(request.title())
                .imageName(imageName)
                .linkUrl(request.linkUrl())
                .isActive(Boolean.TRUE.equals(request.isActive()))
                .displayOrder(request.displayOrder() != null ? request.displayOrder() : 0)
                .build();

        return mapToBannerResponse(bannerRepository.save(banner));
    }

    @Override
    @Transactional
    public BannerResponse updateBanner(Long id, BannerRequest request) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new SystemException(SystemErrorCode.DATA_NOT_FOUND));

        banner.setTitle(request.title());
        banner.setLinkUrl(request.linkUrl());
        
        if (request.isActive() != null) {
            banner.setIsActive(request.isActive());
        }
        if (request.displayOrder() != null) {
            banner.setDisplayOrder(request.displayOrder());
        }

        if (request.file() != null && !request.file().isEmpty()) {
            if (banner.getImageName() != null) {
                fileService.deleteFile(banner.getImageName());
            }
            banner.setImageName(fileService.uploadFile(request.file()));
        }

        return mapToBannerResponse(bannerRepository.save(banner));
    }

    @Override
    @Transactional
    public void deleteBanner(Long id) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new SystemException(SystemErrorCode.DATA_NOT_FOUND));
                
        if (banner.getImageName() != null) {
            fileService.deleteFile(banner.getImageName());
        }
        bannerRepository.delete(banner);
    }

    @Override
    @Transactional
    public void changeBannerStatus(Long id, boolean isActive) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new SystemException(SystemErrorCode.DATA_NOT_FOUND));
        banner.setIsActive(isActive);
        bannerRepository.save(banner);
    }

    private StoreSettingResponse mapToStoreSettingResponse(StoreSetting setting) {
        return StoreSettingResponse.builder()
                .id(setting.getId())
                .storeName(setting.getStoreName())
                .hotline(setting.getHotline())
                .isOpen(setting.getIsOpen())
                .baseShippingFee(setting.getBaseShippingFee())
                .freeShipThreshold(setting.getFreeShipThreshold())
                .build();
    }

    private BannerResponse mapToBannerResponse(Banner banner) {
        String imageUrl = banner.getImageName() != null ? serverUrl + "/api/v1/media/" + banner.getImageName() : null;
        return BannerResponse.builder()
                .id(banner.getId())
                .title(banner.getTitle())
                .imageUrl(imageUrl)
                .linkUrl(banner.getLinkUrl())
                .isActive(banner.getIsActive())
                .displayOrder(banner.getDisplayOrder())
                .build();
    }
}
