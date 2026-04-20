package com.fm.foodmanagementsystem.modules.product_service.models.repositories;

import com.fm.foodmanagementsystem.modules.product_service.models.entities.OptionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OptionItemRepository extends JpaRepository<OptionItem, Long> {
    List<OptionItem> findAllByOptionGroupId(Long groupId);
}
