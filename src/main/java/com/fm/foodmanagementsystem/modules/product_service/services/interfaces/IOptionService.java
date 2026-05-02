package com.fm.foodmanagementsystem.modules.product_service.services.interfaces;

import com.fm.foodmanagementsystem.modules.product_service.resources.requests.OptionGroupRequest;
import com.fm.foodmanagementsystem.modules.product_service.resources.responses.OptionGroupResponse;
import java.util.List;

public interface IOptionService {
    OptionGroupResponse createOptionGroup(Long foodId, OptionGroupRequest request);
    void deleteOptionGroup(Long groupId);
    /** Khách: chỉ khi món đang bán (đồng bộ ẩn với chi tiết món). */
    List<OptionGroupResponse> getOptionsByFoodId(Long foodId);

    /** Admin: luôn trả về nếu món tồn tại. */
    List<OptionGroupResponse> getOptionsByFoodIdForAdmin(Long foodId);
    OptionGroupResponse updateOptionGroup(Long groupId, OptionGroupRequest request);
}