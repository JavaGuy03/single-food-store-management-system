package com.fm.foodmanagementsystem.modules.product_service.mappers;

import com.fm.foodmanagementsystem.modules.product_service.models.entities.OptionGroup;
import com.fm.foodmanagementsystem.modules.product_service.models.entities.OptionItem;
import com.fm.foodmanagementsystem.modules.product_service.resources.responses.OptionGroupResponse;
import com.fm.foodmanagementsystem.modules.product_service.resources.responses.OptionItemResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class OptionMapper {

    public OptionItemResponse mapToItemResponse(OptionItem item) {
        return OptionItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .priceAdjustment(item.getPriceAdjustment())
                .build();
    }

    public OptionGroupResponse mapToGroupResponse(OptionGroup group) {
        List<OptionItemResponse> itemResponses = group.getItems() != null
                ? group.getItems().stream().map(this::mapToItemResponse).toList()
                : Collections.emptyList();

        return OptionGroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .minSelect(group.getMinSelect())
                .maxSelect(group.getMaxSelect())
                .foodId(group.getFood() != null ? group.getFood().getId() : null)
                .items(itemResponses)
                .build();
    }
}