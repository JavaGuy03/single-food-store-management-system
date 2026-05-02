package com.fm.foodmanagementsystem.modules.product_service.services.imps;

import com.fm.foodmanagementsystem.core.exception.SystemException;
import com.fm.foodmanagementsystem.core.exception.enums.SystemErrorCode;
import com.fm.foodmanagementsystem.modules.product_service.mappers.OptionMapper;
import com.fm.foodmanagementsystem.modules.product_service.models.entities.Food;
import com.fm.foodmanagementsystem.modules.product_service.models.entities.OptionGroup;
import com.fm.foodmanagementsystem.modules.product_service.models.entities.OptionItem;
import com.fm.foodmanagementsystem.modules.product_service.models.repositories.FoodRepository;
import com.fm.foodmanagementsystem.modules.product_service.models.repositories.OptionGroupRepository;
import com.fm.foodmanagementsystem.modules.product_service.resources.requests.OptionGroupRequest;
import com.fm.foodmanagementsystem.modules.product_service.resources.responses.OptionGroupResponse;
import com.fm.foodmanagementsystem.modules.product_service.services.interfaces.IOptionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OptionService implements IOptionService {

    OptionGroupRepository optionGroupRepository;
    FoodRepository foodRepository;
    OptionMapper optionMapper;

    @Override
    @Transactional
    public OptionGroupResponse createOptionGroup(Long foodId, OptionGroupRequest request) {
        Food food = foodRepository.findById(foodId)
                .orElseThrow(() -> new SystemException(SystemErrorCode.DATA_NOT_FOUND));

        if (request.minSelect() > request.maxSelect()) {
            throw new SystemException(SystemErrorCode.INVALID_PARAMETER);
        }

        OptionGroup group = new OptionGroup();
        group.setName(request.name());
        group.setMinSelect(request.minSelect());
        group.setMaxSelect(request.maxSelect());
        group.setFood(food);

        if (request.items() != null && !request.items().isEmpty()) {
            Set<OptionItem> items = new HashSet<>(request.items().stream().map(itemReq -> {
                OptionItem item = new OptionItem();
                item.setName(itemReq.name());
                item.setPriceAdjustment(itemReq.priceAdjustment());
                item.setOptionGroup(group);
                return item;
            }).toList());
            group.setItems(items);
        }

        OptionGroup savedGroup = optionGroupRepository.save(group);
        return optionMapper.mapToGroupResponse(savedGroup);
    }

    @Override
    @Transactional
    public void deleteOptionGroup(Long groupId) {
        if (!optionGroupRepository.existsById(groupId)) {
            throw new SystemException(SystemErrorCode.DATA_NOT_FOUND);
        }
        optionGroupRepository.deleteById(groupId);
    }

    @Override
    public List<OptionGroupResponse> getOptionsByFoodId(Long foodId) {
        Food food = foodRepository.findById(foodId)
                .orElseThrow(() -> new SystemException(SystemErrorCode.DATA_NOT_FOUND));
        if (!food.getIsAvailable()) {
            throw new SystemException(SystemErrorCode.DATA_NOT_FOUND);
        }
        return optionGroupRepository.findAllByFoodId(foodId).stream()
                .map(optionMapper::mapToGroupResponse)
                .toList();
    }

    @Override
    public List<OptionGroupResponse> getOptionsByFoodIdForAdmin(Long foodId) {
        if (!foodRepository.existsById(foodId)) {
            throw new SystemException(SystemErrorCode.DATA_NOT_FOUND);
        }
        return optionGroupRepository.findAllByFoodId(foodId).stream()
                .map(optionMapper::mapToGroupResponse)
                .toList();
    }

    @Override
    @Transactional
    public OptionGroupResponse updateOptionGroup(Long groupId, OptionGroupRequest request) {
        OptionGroup group = optionGroupRepository.findById(groupId)
                .orElseThrow(() -> new SystemException(SystemErrorCode.DATA_NOT_FOUND));

        // Cập nhật thông tin Group
        group.setName(request.name());
        if (request.minSelect() > request.maxSelect()) {
            throw new SystemException(SystemErrorCode.INVALID_PARAMETER);
        }
        group.setMinSelect(request.minSelect());
        group.setMaxSelect(request.maxSelect());

        // Cập nhật Items (Xoá cũ, Thêm mới)
        // Lưu ý: Nhờ có cascade = CascadeType.ALL và orphanRemoval = true (bác nên thêm orphanRemoval vào Entity),
        // hibernate sẽ tự động xoá các item cũ bị loại khỏi list.
        if (group.getItems() != null) {
            group.getItems().clear();
        }

        if (request.items() != null && !request.items().isEmpty()) {
            Set<OptionItem> newItems = new HashSet<>(request.items().stream().map(itemReq -> {
                OptionItem item = new OptionItem();
                item.setName(itemReq.name());
                item.setPriceAdjustment(itemReq.priceAdjustment());
                item.setOptionGroup(group);
                return item;
            }).toList());

            group.getItems().addAll(newItems);
        }

        OptionGroup savedGroup = optionGroupRepository.save(group);
        return optionMapper.mapToGroupResponse(savedGroup);
    }
}