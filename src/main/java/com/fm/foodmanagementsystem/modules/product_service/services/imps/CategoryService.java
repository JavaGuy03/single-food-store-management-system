package com.fm.foodmanagementsystem.modules.product_service.services.imps;

import com.fm.foodmanagementsystem.core.exception.SystemException;
import com.fm.foodmanagementsystem.core.exception.enums.SystemErrorCode;
import com.fm.foodmanagementsystem.core.services.interfaces.IFileService;
import com.fm.foodmanagementsystem.modules.product_service.mappers.CategoryMapper;
import com.fm.foodmanagementsystem.modules.product_service.models.entities.Category;
import com.fm.foodmanagementsystem.modules.product_service.models.repositories.CategoryRepository;
import com.fm.foodmanagementsystem.modules.product_service.models.repositories.FoodRepository;
import com.fm.foodmanagementsystem.modules.product_service.resources.requests.CategoryRequest;
import com.fm.foodmanagementsystem.modules.product_service.resources.responses.CategoryResponse;
import com.fm.foodmanagementsystem.modules.product_service.services.interfaces.ICategoryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryService implements ICategoryService {

    CategoryRepository categoryRepository;
    CategoryMapper categoryMapper;
    IFileService fileService;
    FoodRepository foodRepository;

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByName(request.name())) {
            throw new SystemException(SystemErrorCode.DATA_IS_IN_USE);
        }

        String imageName = null;
        if (request.file() != null && !request.file().isEmpty()) {
            imageName = fileService.uploadFile(request.file());
        }

        Category category = Category.builder()
                .name(request.name())
                .description(request.description())
                .imageName(imageName)
                .build();

        return categoryMapper.mapToResponse(categoryRepository.save(category));
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream().map(categoryMapper::mapToResponse).toList();
    }

    @Override
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new SystemException(SystemErrorCode.DATA_NOT_FOUND));
        return categoryMapper.mapToResponse(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new SystemException(SystemErrorCode.DATA_NOT_FOUND));

        boolean hasFoods = foodRepository.existsByCategoryId(id);
        if (hasFoods) {
            throw new SystemException(SystemErrorCode.DATA_IS_IN_USE);
        }

        if (category.getImageName() != null) {
            fileService.deleteFile(category.getImageName());
        }

        categoryRepository.deleteById(id);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new SystemException(SystemErrorCode.DATA_NOT_FOUND));

        if (!category.getName().equals(request.name()) && categoryRepository.existsByName(request.name())) {
            throw new SystemException(SystemErrorCode.DATA_IS_IN_USE);
        }

        category.setName(request.name());
        category.setDescription(request.description());

        if (request.file() != null && !request.file().isEmpty()) {
            if (category.getImageName() != null) {
                fileService.deleteFile(category.getImageName());
            }
            String newImageName = fileService.uploadFile(request.file());
            category.setImageName(newImageName);
        }

        return categoryMapper.mapToResponse(categoryRepository.save(category));
    }
}