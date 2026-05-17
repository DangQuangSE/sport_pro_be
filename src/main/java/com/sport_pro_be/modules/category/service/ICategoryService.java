package com.sport_pro_be.modules.category.service;

import com.sport_pro_be.modules.category.dto.CategoryRequest;
import com.sport_pro_be.modules.category.dto.CategoryResponse;
import com.sport_pro_be.modules.category.dto.CategoryTreeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ICategoryService {
    CategoryResponse createCategory(CategoryRequest request);
    CategoryResponse updateCategory(Long id, CategoryRequest request);
    void deleteCategory(Long id);
    CategoryResponse getCategoryBySlug(String slug);
    List<CategoryTreeResponse> getCategoryTree();
    Page<CategoryResponse> getCategories(Pageable pageable, String search);
    void updateStatus(Long id, boolean isActive);
}

