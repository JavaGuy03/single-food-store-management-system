package com.fm.foodmanagementsystem.modules.product_service.models.repositories;

import com.fm.foodmanagementsystem.modules.product_service.models.entities.Food;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {
    boolean existsByName(String name);
    List<Food> findAllByCategoryId(Long categoryId);
}
