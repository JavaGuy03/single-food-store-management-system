package com.fm.foodmanagementsystem.modules.product_service.mappers;

import com.fm.foodmanagementsystem.modules.product_service.models.entities.Food;
import com.fm.foodmanagementsystem.modules.product_service.models.entities.OptionGroup;
import com.fm.foodmanagementsystem.modules.product_service.resources.responses.FoodResponse;
import com.fm.foodmanagementsystem.modules.product_service.resources.responses.OptionGroupResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FoodMapper {

    @Value("${openapi.service.url:http://localhost:8080}")
    private String serverUrl;

    private final OptionMapper optionMapper; // Inject OptionMapper vào đây

    public FoodResponse mapToResponse(Food food, List<OptionGroup> optionGroups) {
        String imageUrl = (food.getImageName() != null)
                ? serverUrl + "/api/v1/media/" + food.getImageName()
                : null;

        List<OptionGroupResponse> groupResponses = optionGroups != null
                ? optionGroups.stream().map(optionMapper::mapToGroupResponse).toList()
                : Collections.emptyList();

        return FoodResponse.builder()
                .id(food.getId())
                .name(food.getName())
                .description(food.getDescription())
                .price(food.getPrice())
                .isAvailable(food.getIsAvailable())
                .imageUrl(imageUrl)
                .categoryId(food.getCategory() != null ? food.getCategory().getId() : null)
                .categoryName(food.getCategory() != null ? food.getCategory().getName() : null)
                .optionGroups(groupResponses) // Ép luôn mảng Options vào đây
                .build();
    }
}