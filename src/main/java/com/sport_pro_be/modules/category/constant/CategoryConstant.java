package com.sport_pro_be.modules.category.constant;

public class CategoryConstant {
    private CategoryConstant() {
    }

    // Success Messages
    public static final String CREATE_CATEGORY_SUCCESS = "Category created successfully";
    public static final String UPDATE_CATEGORY_SUCCESS = "Category updated successfully";
    public static final String DELETE_CATEGORY_SUCCESS = "Category deleted successfully";
    public static final String GET_CATEGORY_TREE_SUCCESS = "Category tree retrieved successfully";
    public static final String GET_CATEGORY_DETAIL_SUCCESS = "Category details retrieved successfully";
    public static final String GET_CATEGORIES_SUCCESS = "Categories retrieved successfully";
    public static final String UPDATE_STATUS_SUCCESS = "Category status updated successfully";

    // Error Messages
    public static final String CATEGORY_NOT_FOUND_ID = "Category not found with id: %d";
    public static final String CATEGORY_NOT_FOUND_SLUG = "Category not found with slug: %s";
    public static final String PARENT_CATEGORY_NOT_FOUND = "Parent category not found with id: %d";
    public static final String CIRCULAR_REFERENCE = "A category cannot be its own parent";
    public static final String CATEGORY_NAME_ALREADY_EXISTS = "Category name already exists";

    // Validation Messages
    public static final String NAME_CANNOT_BE_BLANK = "Category name cannot be blank";
    public static final String NAME_SIZE = "Category name must be between 2 and 100 characters";
}

