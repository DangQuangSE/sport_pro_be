package com.sport_pro_be.modules.order.constant;

public class OrderMessageConstant {

    private OrderMessageConstant() {
    }

    // Success Messages
    public static final String ORDER_PLACED_SUCCESS = "Order placed successfully";
    public static final String USER_ORDERS_RETRIEVED = "User orders retrieved successfully";
    public static final String ORDER_DETAILS_RETRIEVED = "Order details retrieved successfully";

    // Error Messages
    public static final String USER_NOT_FOUND = "User not found";
    public static final String CART_EMPTY = "Cart is empty";
    public static final String INSUFFICIENT_STOCK = "Insufficient stock for product: %s - Size: %s";
    public static final String ORDER_NOT_FOUND = "Order not found or does not belong to the user";
}
