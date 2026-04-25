package com.fm.foodmanagementsystem.modules.interaction_service.controllers;

import org.springframework.data.domain.Page;
import com.fm.foodmanagementsystem.core.response.ApiResponse;
import com.fm.foodmanagementsystem.modules.interaction_service.resources.requests.ReviewRequest;
import com.fm.foodmanagementsystem.modules.interaction_service.resources.responses.FavoriteResponse;
import com.fm.foodmanagementsystem.modules.interaction_service.resources.responses.ReviewResponse;
import com.fm.foodmanagementsystem.modules.interaction_service.services.interfaces.IInteractionService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/interactions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InteractionController {

    IInteractionService interactionService;

    // --- Reviews ---
    @PostMapping("/reviews")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ReviewResponse> createReview(@Valid @RequestBody ReviewRequest request) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.<ReviewResponse>builder()
                .result(interactionService.createReview(userId, request))
                .build();
    }

    @GetMapping("/reviews")
    public ApiResponse<Page<ReviewResponse>> getAllReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<Page<ReviewResponse>>builder()
                .result(interactionService.getAllReviews(page, size))
                .build();
    }

    @GetMapping("/foods/{foodId}/rating")
    public ApiResponse<Double> getFoodAverageRating(@PathVariable Long foodId) {
        return ApiResponse.<Double>builder()
                .result(interactionService.getFoodAverageRating(foodId))
                .build();
    }

    // --- Favorites ---
    @PostMapping("/favorites/{foodId}/toggle")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> toggleFavorite(@PathVariable Long foodId) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        interactionService.toggleFavorite(userId, foodId);
        return ApiResponse.<Void>builder().build();
    }

    @GetMapping("/favorites/my")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Page<FavoriteResponse>> getMyFavorites(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.<Page<FavoriteResponse>>builder()
                .result(interactionService.getMyFavorites(userId, page, size))
                .build();
    }

    @GetMapping("/favorites/{foodId}/check")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Boolean> checkIsFavorite(@PathVariable Long foodId) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.<Boolean>builder()
                .result(interactionService.checkIsFavorite(userId, foodId))
                .build();
    }
}
