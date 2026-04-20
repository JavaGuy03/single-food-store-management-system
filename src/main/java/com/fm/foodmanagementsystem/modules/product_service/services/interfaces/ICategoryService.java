package com.fm.foodmanagementsystem.modules.product_service.services.interfaces;

import com.fm.foodmanagementsystem.modules.product_service.resources.requests.CategoryRequest;
import com.fm.foodmanagementsystem.modules.product_service.resources.responses.CategoryResponse;
import java.util.List;

public interface ICategoryService {
    CategoryResponse createCategory(CategoryRequest request);
    List<CategoryResponse> getAllCategories();
    CategoryResponse getCategoryById(Long id);
    void deleteCategory(Long id);
}