package com.fm.foodmanagementsystem.modules.setting_service.models.repositories;

import com.fm.foodmanagementsystem.modules.setting_service.models.entities.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Long> {
    List<Banner> findAllByIsActiveTrueOrderByDisplayOrderAsc();
}
