package com.sport_pro_be.modules.inventory.constant;

public class InventoryMessageConstant {

    private InventoryMessageConstant() {}

    // Exception messages
    public static final String VARIANT_NOT_FOUND = "Product variant not found";
    public static final String INSUFFICIENT_STOCK = "Insufficient stock for [%s]: requested %d, available %d";
    public static final String STOCK_CONFLICT = "Stock update conflict, please try again";
    public static final String RESTOCK_QUANTITY_POSITIVE = "Restock quantity must be at least 1";

    // Success messages
    public static final String STOCK_ADJUSTED = "Stock adjusted successfully";
    public static final String LOW_STOCK_RETRIEVED = "Low stock variants retrieved successfully";
}
