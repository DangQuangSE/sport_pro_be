package com.sport_pro_be.modules.review.constant;

public class ReviewMessageConstant {
    private ReviewMessageConstant() {}

    public static final String CREATE_SUCCESS = "Review submitted successfully";
    public static final String UPDATE_SUCCESS = "Review updated successfully";
    public static final String DELETE_SUCCESS = "Review deleted successfully";
    public static final String NOT_FOUND = "Review not found";
    public static final String ORDER_ITEM_NOT_FOUND = "Order item not found";
    public static final String ORDER_NOT_DELIVERED = "You can only review products from delivered orders";
    public static final String ALREADY_REVIEWED = "You have already reviewed this item";
    public static final String UNAUTHORIZED_EDIT = "You are not authorized to edit this review";
    public static final String PRODUCT_NOT_FOUND = "Product not found";

    // Validation
    public static final String RATING_REQUIRED = "Rating is required";
    public static final String RATING_RANGE = "Rating must be between 1 and 5";
    public static final String COMMENT_REQUIRED = "Comment is required";
}
