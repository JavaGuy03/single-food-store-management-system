package com.fm.foodmanagementsystem.modules.product_service.models.repositories;

import com.fm.foodmanagementsystem.modules.product_service.models.entities.Food;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {
    boolean existsByName(String name);

    @EntityGraph(attributePaths = {"category", "optionGroups", "optionGroups.items"})
    Optional<Food> findById(Long id);

    @EntityGraph(attributePaths = {"category", "optionGroups", "optionGroups.items"})
    List<Food> findAllByCategoryId(Long categoryId);

    @EntityGraph(attributePaths = {"category", "optionGroups", "optionGroups.items"})
    List<Food> findAllByIsAvailableTrue();

    @EntityGraph(attributePaths = {"category", "optionGroups", "optionGroups.items"})
    @Query("SELECT f FROM Food f")
    List<Food> findAllWithGraphs();

    boolean existsByCategoryId(Long categoryId);
    // Đếm số lượng món ăn đang mở bán
    long countByIsAvailableTrue();
}
