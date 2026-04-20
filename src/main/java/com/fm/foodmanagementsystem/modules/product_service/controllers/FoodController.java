package com.fm.foodmanagementsystem.modules.product_service.controllers;

import com.fm.foodmanagementsystem.core.response.ApiResponse;
import com.fm.foodmanagementsystem.modules.product_service.resources.requests.FoodRequest;
import com.fm.foodmanagementsystem.modules.product_service.resources.responses.FoodResponse;
import com.fm.foodmanagementsystem.modules.product_service.services.interfaces.IFoodService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/foods")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FoodController {

    IFoodService foodService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<FoodResponse> create(@ModelAttribute @Valid FoodRequest request) {
        return ApiResponse.<FoodResponse>builder()
                .message("Tạo món ăn thành công")
                .result(foodService.createFood(request))
                .build();
    }

    @GetMapping
    public ApiResponse<List<FoodResponse>> getAll() {
        return ApiResponse.<List<FoodResponse>>builder()
                .result(foodService.getAllFoods())
                .build();
    }

    @GetMapping("/category/{categoryId}")
    public ApiResponse<List<FoodResponse>> getByCategoryId(@PathVariable Long categoryId) {
        return ApiResponse.<List<FoodResponse>>builder()
                .result(foodService.getFoodsByCategory(categoryId))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<FoodResponse> getById(@PathVariable Long id) {
        return ApiResponse.<FoodResponse>builder()
                .result(foodService.getFoodById(id))
                .build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> changeStatus(@PathVariable Long id, @RequestParam boolean isAvailable) {
        foodService.changeFoodStatus(id, isAvailable);
        return ApiResponse.<Void>builder().message("Cập nhật trạng thái món ăn thành công").build();
    }
}