package com.fm.foodmanagementsystem.modules.product_service.mappers;

import com.fm.foodmanagementsystem.modules.product_service.models.entities.Category;
import com.fm.foodmanagementsystem.modules.product_service.resources.responses.CategoryResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    @Value("${openapi.service.url:http://localhost:8080}")
    private String serverUrl;

    public CategoryResponse mapToResponse(Category category) {
        String imageUrl = (category.getImageName() != null)
                ? serverUrl + "/api/v1/media/" + category.getImageName()
                : null;

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .imageUrl(imageUrl)
                .build();
    }
}