package com.fm.foodmanagementsystem.modules.interaction_service.models.repositories;

import com.fm.foodmanagementsystem.modules.interaction_service.models.entities.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<Review> findAllByFoodIdOrderByCreatedAtDesc(Long foodId, Pageable pageable);

    // C-6 FIX: Direct FK query — no more Cartesian JOIN, no inflation, accurate results
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.food.id = :foodId")
    Double getAverageRatingByFoodId(Long foodId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.food.id = :foodId")
    Long countByFoodId(Long foodId);

    // C-6: Check if a review already exists for this (order, food) combination
    boolean existsByOrderIdAndFoodId(String orderId, Long foodId);
}
