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

    // Admin: Xem tất cả món bao gồm cả đã ẩn/ngừng bán
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<FoodResponse>> getAllForAdmin() {
        return ApiResponse.<List<FoodResponse>>builder()
                .result(foodService.getAllFoodsAdmin())
                .build();
    }

    @GetMapping("/category/{categoryId}")
    public ApiResponse<List<FoodResponse>> getByCategoryId(@PathVariable Long categoryId) {
        return ApiResponse.<List<FoodResponse>>builder()
                .result(foodService.getFoodsByCategory(categoryId))
                .build();
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<FoodResponse> getByIdForAdmin(@PathVariable Long id) {
        return ApiResponse.<FoodResponse>builder()
                .result(foodService.getFoodByIdForAdmin(id))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<FoodResponse> getById(@PathVariable Long id) {
        return ApiResponse.<FoodResponse>builder()
                .result(foodService.getFoodByIdForCustomer(id))
                .build();
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<FoodResponse> update(
            @PathVariable Long id,
            @ModelAttribute @Valid FoodRequest request) {
        return ApiResponse.<FoodResponse>builder()
                .message("Cập nhật món ăn thành công")
                .result(foodService.updateFood(id, request))
                .build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> changeStatus(@PathVariable Long id, @RequestParam boolean isAvailable) {
        foodService.changeFoodStatus(id, isAvailable);
        return ApiResponse.<Void>builder().message("Cập nhật trạng thái món ăn thành công").build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        foodService.deleteFood(id);
        return ApiResponse.<Void>builder()
                .message("Đã ngừng bán món ăn thành công") // Thông báo mềm mại vì ta dùng Soft Delete
                .build();
    }
}