package com.fm.foodmanagementsystem.modules.product_service.services.interfaces;

import com.fm.foodmanagementsystem.modules.product_service.resources.requests.OptionGroupRequest;
import com.fm.foodmanagementsystem.modules.product_service.resources.responses.OptionGroupResponse;
import java.util.List;

public interface IOptionService {
    OptionGroupResponse createOptionGroup(Long foodId, OptionGroupRequest request);
    void deleteOptionGroup(Long groupId);
    List<OptionGroupResponse> getOptionsByFoodId(Long foodId);
}