package com.sport_pro_be.modules.order.constant;

public class OrderMessageConstant {

    private OrderMessageConstant() {
    }

    // Success Messages
    public static final String ORDER_PLACED_SUCCESS = "Order placed successfully";
    public static final String USER_ORDERS_RETRIEVED = "User orders retrieved successfully";
    public static final String ORDER_DETAILS_RETRIEVED = "Order details retrieved successfully";
    public static final String ORDER_STATUS_UPDATED = "Order status updated successfully";
    public static final String ALL_ORDERS_RETRIEVED = "All orders retrieved successfully";

    // Error Messages
    public static final String USER_NOT_FOUND = "User not found";
    public static final String CART_EMPTY = "Cart is empty";
    public static final String INSUFFICIENT_STOCK = "Insufficient stock for product: %s - Size: %s";
    public static final String ORDER_NOT_FOUND = "Order not found or does not belong to the user";
    public static final String INVALID_SELECTED_ITEMS = "No valid items selected from cart";
    public static final String RETURN_REQUESTED_SUCCESS = "Return request submitted successfully";
    public static final String RETURN_NOT_FOUND = "Return request not found";
    public static final String RETURN_ALREADY_EXISTS = "A return request already exists for this order";
    public static final String INVALID_ORDER_FOR_RETURN = "This order is not eligible for return (must be DELIVERED)";
    public static final String STATUS_REQUIRED = "Status is required";
    public static final String ORDER_ID_REQUIRED = "Order ID is required";
    public static final String REASON_REQUIRED = "Reason is required";
    public static final String BANK_INFO_REQUIRED = "Bank account info is required for refund processing";
}
