package com.fm.foodmanagementsystem.modules.interaction_service.models.repositories;

import com.fm.foodmanagementsystem.modules.interaction_service.models.entities.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    Page<Favorite> findAllByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    Optional<Favorite> findByUserIdAndFoodId(String userId, Long foodId);
    boolean existsByUserIdAndFoodId(String userId, Long foodId);
}
