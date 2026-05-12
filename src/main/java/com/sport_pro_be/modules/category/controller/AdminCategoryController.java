package com.sport_pro_be.modules.category.controller;

import com.sport_pro_be.modules.category.constant.CategoryConstant;
import com.sport_pro_be.modules.category.dto.CategoryRequest;
import com.sport_pro_be.modules.category.dto.CategoryResponse;
import com.sport_pro_be.modules.category.service.ICategoryService;
import com.sport_pro_be.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final ICategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        return ApiResponse.of(CategoryConstant.CREATE_CATEGORY_SUCCESS, categoryService.createCategory(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<CategoryResponse> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return ApiResponse.of(CategoryConstant.UPDATE_CATEGORY_SUCCESS, categoryService.updateCategory(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ApiResponse.of(CategoryConstant.DELETE_CATEGORY_SUCCESS, null);
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestParam boolean isActive) {
        categoryService.updateStatus(id, isActive);
        return ApiResponse.of(CategoryConstant.UPDATE_STATUS_SUCCESS, null);
    }
}

