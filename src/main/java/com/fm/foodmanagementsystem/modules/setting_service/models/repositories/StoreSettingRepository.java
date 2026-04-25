package com.fm.foodmanagementsystem.modules.setting_service.models.repositories;

import com.fm.foodmanagementsystem.modules.setting_service.models.entities.StoreSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreSettingRepository extends JpaRepository<StoreSetting, Long> {
}
