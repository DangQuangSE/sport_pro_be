package com.sport_pro_be.modules.cart.constant;

public class CartMessageConstant {
    // Validation messages
    public static final String VARIANT_ID_REQUIRED = "Variant ID is required";
    public static final String QUANTITY_REQUIRED = "Quantity is required";
    public static final String QUANTITY_MIN = "Quantity must be greater than or equal to 0";

    // Exception messages
    public static final String VARIANT_NOT_FOUND = "Product variant not found or inactive";
    public static final String OUT_OF_STOCK = "Requested quantity exceeds current stock";
    public static final String CART_ITEM_NOT_FOUND = "Item not found in cart";

    // Success messages
    public static final String CART_RETRIEVED = "Cart retrieved successfully";
    public static final String CART_UPDATED = "Cart updated successfully";
    public static final String ITEM_REMOVED = "Item removed from cart successfully";
}
