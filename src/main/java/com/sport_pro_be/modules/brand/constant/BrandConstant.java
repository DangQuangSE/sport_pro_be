package com.sport_pro_be.modules.brand.constant;

public class BrandConstant {
    private BrandConstant() {
    }

    // Success Messages
    public static final String CREATE_BRAND_SUCCESS = "Brand created successfully";
    public static final String UPDATE_BRAND_SUCCESS = "Brand updated successfully";
    public static final String DELETE_BRAND_SUCCESS = "Brand deleted successfully";
    public static final String GET_BRAND_DETAIL_SUCCESS = "Brand details retrieved successfully";
    public static final String GET_BRANDS_SUCCESS = "Brands retrieved successfully";
    public static final String UPDATE_STATUS_SUCCESS = "Brand status updated successfully";

    // Error Messages
    public static final String BRAND_NOT_FOUND_ID = "Brand not found with id: %d";
    public static final String BRAND_NOT_FOUND_SLUG = "Brand not found with slug: %s";

    // Validation Messages
    public static final String NAME_CANNOT_BE_BLANK = "Brand name cannot be blank";
    public static final String NAME_SIZE = "Brand name must be between 2 and 100 characters";
}

