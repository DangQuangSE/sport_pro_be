package com.sport_pro_be.modules.product.constant;

public class ProductMessageConstant {
    private ProductMessageConstant() {}

    // Validation messages
    public static final String NAME_REQUIRED = "Name is required";
    public static final String CATEGORY_ID_REQUIRED = "Category ID is required";
    public static final String BRAND_ID_REQUIRED = "Brand ID is required";
    public static final String GENDER_REQUIRED = "Gender is required";
    public static final String STATUS_REQUIRED = "Status is required";
    public static final String SKU_REQUIRED = "SKU is required";
    public static final String SIZE_REQUIRED = "Size is required";
    public static final String COLOR_REQUIRED = "Color is required";
    public static final String PRICE_REQUIRED = "Price is required";
    public static final String PRICE_POSITIVE = "Price must be greater than 0";
    public static final String STOCK_NON_NEGATIVE = "Stock quantity cannot be negative";
    public static final String IMAGE_URL_REQUIRED = "Image URL is required";

    // Exception messages
    public static final String PRODUCT_NOT_FOUND = "Product not found";
    public static final String CATEGORY_NOT_FOUND = "Category not found";
    public static final String BRAND_NOT_FOUND = "Brand not found";
    public static final String VARIANT_NOT_FOUND = "Product Variant not found";
    public static final String SKU_ALREADY_EXISTS = "SKU already exists";
    public static final String IMAGE_NOT_FOUND = "Product Image not found";

    // Success messages
    public static final String SUCCESS = "Success";
    public static final String PRODUCT_CREATED = "Product created successfully";
    public static final String PRODUCT_UPDATED = "Product updated successfully";
    public static final String PRODUCT_DELETED = "Product deleted successfully";
    public static final String VARIANT_CREATED = "Variant created successfully";
    public static final String VARIANT_UPDATED = "Variant updated successfully";
    public static final String VARIANT_DELETED = "Variant deleted successfully";
    public static final String IMAGE_ADDED = "Image added successfully";
    public static final String IMAGE_DELETED = "Image deleted successfully";
}


