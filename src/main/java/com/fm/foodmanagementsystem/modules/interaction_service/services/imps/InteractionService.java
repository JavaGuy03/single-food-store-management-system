package com.fm.foodmanagementsystem.modules.interaction_service.services.imps;

import com.fm.foodmanagementsystem.core.exception.SystemException;
import com.fm.foodmanagementsystem.core.exception.enums.SystemErrorCode;
import org.springframework.data.domain.PageImpl;
import com.fm.foodmanagementsystem.modules.auth_service.models.entities.User;
import com.fm.foodmanagementsystem.modules.auth_service.models.repositories.UserRepository;
import com.fm.foodmanagementsystem.modules.interaction_service.models.entities.Favorite;
import com.fm.foodmanagementsystem.modules.interaction_service.models.entities.Review;
import com.fm.foodmanagementsystem.modules.interaction_service.models.repositories.FavoriteRepository;
import com.fm.foodmanagementsystem.modules.interaction_service.models.repositories.ReviewRepository;
import com.fm.foodmanagementsystem.modules.interaction_service.resources.requests.ReviewRequest;
import com.fm.foodmanagementsystem.modules.interaction_service.resources.responses.FavoriteResponse;
import com.fm.foodmanagementsystem.modules.interaction_service.resources.responses.FoodRatingResponse;
import com.fm.foodmanagementsystem.modules.interaction_service.resources.responses.ReviewResponse;
import com.fm.foodmanagementsystem.modules.interaction_service.services.interfaces.IInteractionService;
import com.fm.foodmanagementsystem.modules.order_service.models.entities.Order;
import com.fm.foodmanagementsystem.modules.order_service.models.enums.OrderStatus;
import com.fm.foodmanagementsystem.modules.order_service.models.repositories.OrderRepository;
import com.fm.foodmanagementsystem.modules.product_service.models.entities.Food;
import com.fm.foodmanagementsystem.modules.product_service.models.repositories.FoodRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InteractionService implements IInteractionService {

    final ReviewRepository reviewRepository;
    final FavoriteRepository favoriteRepository;
    final UserRepository userRepository;
    final OrderRepository orderRepository;
    final FoodRepository foodRepository;

    @Value("${openapi.service.url:http://localhost:8080}")
    String serverUrl;

    @Override
    @Transactional
    public ReviewResponse createReview(String userId, ReviewRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new SystemException(SystemErrorCode.USER_NOT_EXISTED));

        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new SystemException(SystemErrorCode.DATA_NOT_FOUND));

        if (!order.getUserId().equals(userId)) {
            throw new SystemException(SystemErrorCode.UNAUTHORIZED_ACTION);
        }

        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new SystemException(SystemErrorCode.INVALID_PARAMETER); // Chỉ cho phép đánh giá đơn COMPLETED
        }

        // C-6 FIX: Validate the foodId actually belongs to this order’s items
        boolean foodInOrder = order.getOrderItems().stream()
                .anyMatch(item -> item.getFood().getId().equals(request.foodId()));
        if (!foodInOrder) {
            throw new SystemException(SystemErrorCode.INVALID_PARAMETER);
        }

        // C-6: Duplicate check — one review per (order, food)
        if (reviewRepository.existsByOrderIdAndFoodId(order.getId(), request.foodId())) {
            throw new SystemException(SystemErrorCode.DATA_IS_IN_USE);
        }

        Food food = foodRepository.findById(request.foodId())
                .orElseThrow(() -> new SystemException(SystemErrorCode.DATA_NOT_FOUND));

        Review review = Review.builder()
                .user(user)
                .order(order)
                .food(food) // C-6: Set the food FK
                .rating(request.rating())
                .comment(request.comment())
                .build();

        return mapToReviewResponse(reviewRepository.save(review));
    }

    @Override
    public Page<ReviewResponse> getAllReviews(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviews = reviewRepository.findAllByOrderByCreatedAtDesc(pageable);

        List<ReviewResponse> content = reviews.getContent().stream()
                .map(this::mapToReviewResponse)
                .toList();

        return new PageImpl<>(content, pageable, reviews.getTotalElements());
    }

    @Override
    public FoodRatingResponse getFoodRating(Long foodId) {
        Double avg = reviewRepository.getAverageRatingByFoodId(foodId);
        Long count = reviewRepository.countByFoodId(foodId);
        double roundedAvg = avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
        return new FoodRatingResponse(roundedAvg, count != null ? count : 0L);
    }

    @Override
    @Transactional
    public void toggleFavorite(String userId, Long foodId) {
        favoriteRepository.findByUserIdAndFoodId(userId, foodId).ifPresentOrElse(
                favoriteRepository::delete, // Bỏ yêu thích
                () -> {
                    // Thêm yêu thích
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new SystemException(SystemErrorCode.USER_NOT_EXISTED));
                    Food food = foodRepository.findById(foodId)
                            .orElseThrow(() -> new SystemException(SystemErrorCode.DATA_NOT_FOUND));
                    
                    Favorite favorite = Favorite.builder()
                            .user(user)
                            .food(food)
                            .build();
                    favoriteRepository.save(favorite);
                }
        );
    }

    @Override
    public Page<FavoriteResponse> getMyFavorites(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Favorite> favorites = favoriteRepository.findAllByUserIdOrderByCreatedAtDesc(userId, pageable);

        List<FavoriteResponse> content = favorites.getContent().stream()
                .map(this::mapToFavoriteResponse)
                .toList();

        return new PageImpl<>(content, pageable, favorites.getTotalElements());
    }

    @Override
    public boolean checkIsFavorite(String userId, Long foodId) {
        return favoriteRepository.existsByUserIdAndFoodId(userId, foodId);
    }

    private ReviewResponse mapToReviewResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .userId(review.getUser().getId())
                .userFullName(review.getUser().getFirstName() + " " + review.getUser().getLastName())
                .orderId(review.getOrder().getId())
                .foodId(review.getFood().getId())        // C-6
                .foodName(review.getFood().getName())    // C-6
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }

    private FavoriteResponse mapToFavoriteResponse(Favorite favorite) {
        Food food = favorite.getFood();
        String imageUrl = food.getImageName() != null ? serverUrl + "/api/v1/media/" + food.getImageName() : null;
        
        return FavoriteResponse.builder()
                .id(favorite.getId())
                .foodId(food.getId())
                .foodName(food.getName())
                .price(food.getPrice())
                .imageUrl(imageUrl)
                .createdAt(favorite.getCreatedAt())
                .build();
    }
}
