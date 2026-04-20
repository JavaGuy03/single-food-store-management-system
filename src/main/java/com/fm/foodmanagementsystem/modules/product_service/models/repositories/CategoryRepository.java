package com.fm.foodmanagementsystem.modules.product_service.models.repositories;

import com.fm.foodmanagementsystem.modules.product_service.models.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);
}
