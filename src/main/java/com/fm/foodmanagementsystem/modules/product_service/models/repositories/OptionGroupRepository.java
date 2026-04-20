package com.fm.foodmanagementsystem.modules.product_service.models.repositories;

import com.fm.foodmanagementsystem.modules.product_service.models.entities.OptionGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OptionGroupRepository extends JpaRepository<OptionGroup, Long> {
    List<OptionGroup> findAllByFoodId(Long foodId);
}
