package com.fm.foodmanagementsystem.modules.product_service.services.imps;

import com.fm.foodmanagementsystem.core.exception.SystemException;
import com.fm.foodmanagementsystem.core.exception.enums.SystemErrorCode;
import com.fm.foodmanagementsystem.core.services.interfaces.IFileService;
import com.fm.foodmanagementsystem.modules.product_service.mappers.FoodMapper;
import com.fm.foodmanagementsystem.modules.product_service.models.entities.Category;
import com.fm.foodmanagementsystem.modules.product_service.models.entities.Food;
import com.fm.foodmanagementsystem.modules.product_service.models.entities.OptionGroup;
import com.fm.foodmanagementsystem.modules.product_service.models.repositories.CategoryRepository;
import com.fm.foodmanagementsystem.modules.product_service.models.repositories.FoodRepository;
import com.fm.foodmanagementsystem.modules.product_service.models.repositories.OptionGroupRepository;
import com.fm.foodmanagementsystem.modules.product_service.resources.requests.FoodRequest;
import com.fm.foodmanagementsystem.modules.product_service.resources.responses.FoodResponse;
import com.fm.foodmanagementsystem.modules.product_service.services.interfaces.IFoodService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FoodService implements IFoodService {

    FoodRepository foodRepository;
    CategoryRepository categoryRepository;
    OptionGroupRepository optionGroupRepository;
    FoodMapper foodMapper;
    IFileService fileService;

    @Override
    @Transactional
    public FoodResponse createFood(FoodRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new SystemException(SystemErrorCode.DATA_NOT_FOUND));

        String imageName = null;
        if (request.file() != null && !request.file().isEmpty()) {
            imageName = fileService.uploadFile(request.file());
        }

        Food food = Food.builder()
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .isAvailable(true)
                .imageName(imageName)
                .category(category)
                .build();

        return foodMapper.mapToResponse(foodRepository.save(food), null);
    }

    @Override
    public List<FoodResponse> getAllFoods() {
        // M7: Chỉ trả về món ăn đang bán cho khách hàng
        // M8: Tối ưu hóa N+1 bằng EntityGraph, không cần query OptionGroupRepository trong vòng lặp nữa
        return foodRepository.findAllByIsAvailableTrue().stream()
                .map(food -> foodMapper.mapToResponse(food, food.getOptionGroups()))
                .toList();
    }

    @Override
    public List<FoodResponse> getFoodsByCategory(Long categoryId) {
        return foodRepository.findAllByCategoryId(categoryId).stream()
                .map(food -> foodMapper.mapToResponse(food, food.getOptionGroups()))
                .toList();
    }

    @Override
    public FoodResponse getFoodById(Long id) {
        Food food = foodRepository.findById(id)
                .orElseThrow(() -> new SystemException(SystemErrorCode.DATA_NOT_FOUND));
        return foodMapper.mapToResponse(food, food.getOptionGroups());
    }

    @Override
    @Transactional
    public void changeFoodStatus(Long id, boolean isAvailable) {
        Food food = foodRepository.findById(id).orElseThrow(() -> new SystemException(SystemErrorCode.DATA_NOT_FOUND));
        food.setIsAvailable(isAvailable);
        foodRepository.save(food);
    }

    @Override
    @Transactional
    public FoodResponse updateFood(Long id, FoodRequest request) {
        Food food = foodRepository.findById(id)
                .orElseThrow(() -> new SystemException(SystemErrorCode.DATA_NOT_FOUND));

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new SystemException(SystemErrorCode.DATA_NOT_FOUND));

        food.setName(request.name());
        food.setDescription(request.description());
        food.setPrice(request.price());
        food.setCategory(category);

        if (request.file() != null && !request.file().isEmpty()) {
            if (food.getImageName() != null) {
                fileService.deleteFile(food.getImageName());
            }
            String newImageName = fileService.uploadFile(request.file());
            food.setImageName(newImageName);
        }

        Food updatedFood = foodRepository.save(food);
        return foodMapper.mapToResponse(updatedFood, updatedFood.getOptionGroups());
    }

    @Override
    @Transactional
    public void deleteFood(Long id) {
        Food food = foodRepository.findById(id)
                .orElseThrow(() -> new SystemException(SystemErrorCode.DATA_NOT_FOUND));

        // M6: Soft delete - không xóa ảnh vì món ăn vẫn hiển thị trong lịch sử đơn hàng
        food.setIsAvailable(false);
        foodRepository.save(food);
    }
}