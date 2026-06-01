package com.sport_pro_be.modules.category.service;

import com.sport_pro_be.modules.category.constant.CategoryConstant;
import com.sport_pro_be.modules.category.domain.Category;
import com.sport_pro_be.modules.category.dto.CategoryRequest;
import com.sport_pro_be.modules.category.dto.CategoryResponse;
import com.sport_pro_be.modules.category.dto.CategoryTreeResponse;
import com.sport_pro_be.modules.category.repository.CategoryRepository;
import com.sport_pro_be.common.SlugUtils;
import com.sport_pro_be.exception.BadRequestException;
import com.sport_pro_be.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements ICategoryService {

    private final CategoryRepository categoryRepository;
    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        validateParentId(request.getParentId(), null);
        
        Category category = new Category();
        mapRequestToEntity(request, category);
        
        String slug = generateUniqueSlug(request.getName(), null);
        category.setSlug(slug);

        Category savedCategory = categoryRepository.save(category);
        return mapEntityToResponse(savedCategory);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(CategoryConstant.CATEGORY_NOT_FOUND_ID, id)));

        validateParentId(request.getParentId(), id);

        // If name changed, regenerate slug
        if (!category.getName().equals(request.getName())) {
            String slug = generateUniqueSlug(request.getName(), id);
            category.setSlug(slug);
        }

        mapRequestToEntity(request, category);
        Category updatedCategory = categoryRepository.save(category);
        return mapEntityToResponse(updatedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(CategoryConstant.CATEGORY_NOT_FOUND_ID, id)));
        category.setDeletedAt(LocalDateTime.now());
        categoryRepository.save(category);
    }

    @Override
    public CategoryResponse getCategoryBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .map(this::mapEntityToResponse)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(CategoryConstant.CATEGORY_NOT_FOUND_SLUG, slug)));
    }

    @Override
    public List<CategoryTreeResponse> getCategoryTree() {
        List<Category> allCategories = categoryRepository.findAllByIsActiveTrueOrderByDisplayOrderAsc();
        
        // Group by parentId
        Map<Long, List<Category>> childrenMap = allCategories.stream()
                .filter(c -> c.getParentId() != null)
                .collect(Collectors.groupingBy(Category::getParentId));

        // Get root categories
        return allCategories.stream()
                .filter(c -> c.getParentId() == null)
                .map(c -> buildTreeResponse(c, childrenMap))
                .collect(Collectors.toList());
    }

    @Override
    public Page<CategoryResponse> getCategories(Pageable pageable, String search) {
        Page<Category> categoryPage;
        if (search != null && !search.isBlank()) {
            categoryPage = categoryRepository.findAllByNameContainingIgnoreCaseAndIsActiveTrue(search, pageable);
        } else {
            categoryPage = categoryRepository.findAllByIsActiveTrue(pageable);
        }
        return categoryPage.map(this::mapEntityToResponse);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, boolean isActive) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(CategoryConstant.CATEGORY_NOT_FOUND_ID, id)));
        category.setActive(isActive);
        categoryRepository.save(category);
    }

    private void validateParentId(Long parentId, Long currentId) {
        if (parentId == null) {
            return;
        }

        if (parentId.equals(currentId)) {
            throw new BadRequestException(CategoryConstant.CIRCULAR_REFERENCE);
        }

        if (!categoryRepository.existsById(parentId)) {
            throw new ResourceNotFoundException(String.format(CategoryConstant.PARENT_CATEGORY_NOT_FOUND, parentId));
        }
        
        // Optional: Could add deep circular check here if needed
    }

    private String generateUniqueSlug(String name, Long currentId) {
        String baseSlug = SlugUtils.toSlugBase(name);
        String slug = baseSlug;
        int counter = 1;

        while (true) {
            final String currentSlug = slug;
            boolean exists = categoryRepository.findBySlug(currentSlug)
                    .map(c -> !c.getId().equals(currentId))
                    .orElse(false);
            
            if (!exists && !categoryRepository.existsBySlug(currentSlug)) {
                return currentSlug;
            }
            
            slug = baseSlug + "-" + counter++;
        }
    }

    private void mapRequestToEntity(CategoryRequest request, Category category) {
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setParentId(request.getParentId());
        category.setImageUrl(request.getImageUrl());
        if (request.getDisplayOrder() != null) {
            category.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getIsActive() != null) {
            category.setActive(request.getIsActive());
        }
        if (request.getIsCustomizable() != null) {
            category.setCustomizable(request.getIsCustomizable());
        }
    }

    private CategoryResponse mapEntityToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .parentId(category.getParentId())
                .imageUrl(category.getImageUrl())
                .isActive(category.isActive())
                .isCustomizable(category.isCustomizable())
                .displayOrder(category.getDisplayOrder())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    private CategoryTreeResponse buildTreeResponse(Category category, Map<Long, List<Category>> childrenMap) {
        List<CategoryTreeResponse> children = new ArrayList<>();
        if (childrenMap.containsKey(category.getId())) {
            children = childrenMap.get(category.getId()).stream()
                    .map(c -> buildTreeResponse(c, childrenMap))
                    .collect(Collectors.toList());
        }

        return CategoryTreeResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .imageUrl(category.getImageUrl())
                .displayOrder(category.getDisplayOrder())
                .isCustomizable(category.isCustomizable())
                .children(children)
                .build();
    }
}

