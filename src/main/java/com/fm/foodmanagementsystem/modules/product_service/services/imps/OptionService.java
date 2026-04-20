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

import java.util.List;

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

        OptionGroup group = new OptionGroup();
        group.setName(request.name());
        group.setMinSelect(request.minSelect());
        group.setMaxSelect(request.maxSelect());
        group.setFood(food);

        if (request.items() != null && !request.items().isEmpty()) {
            List<OptionItem> items = request.items().stream().map(itemReq -> {
                OptionItem item = new OptionItem();
                item.setName(itemReq.name());
                item.setPriceAdjustment(itemReq.priceAdjustment());
                item.setOptionGroup(group);
                return item;
            }).toList();
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
        return optionGroupRepository.findAllByFoodId(foodId).stream()
                .map(optionMapper::mapToGroupResponse)
                .toList();
    }
}