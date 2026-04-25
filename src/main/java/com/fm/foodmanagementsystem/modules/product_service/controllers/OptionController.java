package com.fm.foodmanagementsystem.modules.product_service.controllers;

import com.fm.foodmanagementsystem.core.response.ApiResponse;
import com.fm.foodmanagementsystem.modules.product_service.resources.requests.OptionGroupRequest;
import com.fm.foodmanagementsystem.modules.product_service.resources.responses.OptionGroupResponse;
import com.fm.foodmanagementsystem.modules.product_service.services.interfaces.IOptionService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/options")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OptionController {

    IOptionService optionService;

    @PostMapping("/food/{foodId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<OptionGroupResponse> createOptionGroup(
            @PathVariable Long foodId,
            @RequestBody @Valid OptionGroupRequest request) {
        return ApiResponse.<OptionGroupResponse>builder()
                .message("Thêm nhóm tuỳ chọn thành công")
                .result(optionService.createOptionGroup(foodId, request))
                .build();
    }

    @GetMapping("/food/{foodId}")
    public ApiResponse<List<OptionGroupResponse>> getByFoodId(@PathVariable Long foodId) {
        return ApiResponse.<List<OptionGroupResponse>>builder()
                .result(optionService.getOptionsByFoodId(foodId))
                .build();
    }

    @DeleteMapping("/groups/{groupId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteOptionGroup(@PathVariable Long groupId) {
        optionService.deleteOptionGroup(groupId);
        return ApiResponse.<Void>builder().message("Xóa nhóm tuỳ chọn thành công").build();
    }

    @PutMapping("/groups/{groupId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<OptionGroupResponse> updateOptionGroup(
            @PathVariable Long groupId,
            @RequestBody @Valid OptionGroupRequest request) {

        return ApiResponse.<OptionGroupResponse>builder()
                .message("Cập nhật nhóm tuỳ chọn thành công")
                .result(optionService.updateOptionGroup(groupId, request))
                .build();
    }
}