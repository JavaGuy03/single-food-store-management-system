package com.fm.foodmanagementsystem.modules.interaction_service.services.interfaces;

import org.springframework.data.domain.Page;
import com.fm.foodmanagementsystem.modules.interaction_service.resources.requests.ReviewRequest;
import com.fm.foodmanagementsystem.modules.interaction_service.resources.responses.FavoriteResponse;
import com.fm.foodmanagementsystem.modules.interaction_service.resources.responses.FoodRatingResponse;
import com.fm.foodmanagementsystem.modules.interaction_service.resources.responses.ReviewResponse;

public interface IInteractionService {
    // Reviews
    ReviewResponse createReview(String userId, ReviewRequest request);
    Page<ReviewResponse> getAllReviews(int page, int size);
    Page<ReviewResponse> getReviewsByFoodId(Long foodId, int page, int size);
    FoodRatingResponse getFoodRating(Long foodId);

    // Favorites
    void toggleFavorite(String userId, Long foodId);
    Page<FavoriteResponse> getMyFavorites(String userId, int page, int size);
    boolean checkIsFavorite(String userId, Long foodId);
}
