package com.fm.foodmanagementsystem.modules.interaction_service.models.repositories;

import com.fm.foodmanagementsystem.modules.interaction_service.models.entities.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    // Custom query to get average rating of a food item by joining with order_items
    @Query("SELECT AVG(r.rating) FROM Review r JOIN r.order o JOIN o.orderItems oi WHERE oi.food.id = :foodId")
    Double getAverageRatingByFoodId(Long foodId);

    // M-2 FIX: DISTINCT prevents count inflation from Cartesian JOIN across multiple order_items
    @Query("SELECT COUNT(DISTINCT r.id) FROM Review r JOIN r.order o JOIN o.orderItems oi WHERE oi.food.id = :foodId")
    Long countByFoodId(Long foodId);

    Optional<Review> findByOrderId(String orderId);
}
