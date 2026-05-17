package com.sport_pro_be.modules.category.controller;

import com.sport_pro_be.modules.category.constant.CategoryConstant;
import com.sport_pro_be.modules.category.dto.CategoryResponse;
import com.sport_pro_be.modules.category.dto.CategoryTreeResponse;
import com.sport_pro_be.modules.category.service.ICategoryService;
import com.sport_pro_be.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final ICategoryService categoryService;

    @GetMapping
    public ApiResponse<Page<CategoryResponse>> getCategories(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) String search) {
        return ApiResponse.of(CategoryConstant.GET_CATEGORIES_SUCCESS, categoryService.getCategories(pageable, search));
    }

    @GetMapping("/tree")
    public ApiResponse<List<CategoryTreeResponse>> getCategoryTree() {
        return ApiResponse.of(CategoryConstant.GET_CATEGORY_TREE_SUCCESS, categoryService.getCategoryTree());
    }

    @GetMapping("/{slug}")
    public ApiResponse<CategoryResponse> getCategoryBySlug(@PathVariable String slug) {
        return ApiResponse.of(CategoryConstant.GET_CATEGORY_DETAIL_SUCCESS, categoryService.getCategoryBySlug(slug));
    }
}

