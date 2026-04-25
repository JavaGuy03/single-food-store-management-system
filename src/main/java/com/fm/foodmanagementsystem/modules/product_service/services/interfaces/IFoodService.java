package com.fm.foodmanagementsystem.modules.product_service.services.interfaces;

import com.fm.foodmanagementsystem.modules.product_service.resources.requests.FoodRequest;
import com.fm.foodmanagementsystem.modules.product_service.resources.responses.FoodResponse;
import java.util.List;

public interface IFoodService {
    FoodResponse createFood(FoodRequest request);
    List<FoodResponse> getAllFoods();
    List<FoodResponse> getFoodsByCategory(Long categoryId);
    FoodResponse getFoodById(Long id);
    void changeFoodStatus(Long id, boolean isAvailable);
    FoodResponse updateFood(Long id, FoodRequest request);
    void deleteFood(Long id);
}