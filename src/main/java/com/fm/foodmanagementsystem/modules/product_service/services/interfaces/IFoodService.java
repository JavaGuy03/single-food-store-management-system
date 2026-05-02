package com.fm.foodmanagementsystem.modules.product_service.services.interfaces;

import com.fm.foodmanagementsystem.modules.product_service.resources.requests.FoodRequest;
import com.fm.foodmanagementsystem.modules.product_service.resources.responses.FoodResponse;
import java.util.List;

public interface IFoodService {
    FoodResponse createFood(FoodRequest request);
    List<FoodResponse> getAllFoods();
    List<FoodResponse> getAllFoodsAdmin(); // Admin: bao gồm cả món ẩn
    List<FoodResponse> getFoodsByCategory(Long categoryId);

    /** Khách: chỉ món đang bán; ngừng bán → không tìm thấy (ẩn khỏi client). */
    FoodResponse getFoodByIdForCustomer(Long id);

    /** Admin: mọi trạng thái hiển thị nếu tồn tại. */
    FoodResponse getFoodByIdForAdmin(Long id);
    void changeFoodStatus(Long id, boolean isAvailable);
    FoodResponse updateFood(Long id, FoodRequest request);
    void deleteFood(Long id);
}