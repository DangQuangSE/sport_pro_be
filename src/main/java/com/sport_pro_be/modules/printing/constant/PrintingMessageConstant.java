package com.sport_pro_be.modules.printing.constant;

public class PrintingMessageConstant {
    private PrintingMessageConstant() {}

    public static final String MATERIAL_NOT_FOUND = "Printing material not found";
    public static final String PRICE_CONFIG_NOT_FOUND = "Printing price configuration not found";
    public static final String COLOR_NOT_FOUND = "Printing color not found";
    public static final String CREATE_SUCCESS = "Created successfully";
    public static final String UPDATE_SUCCESS = "Updated successfully";
    public static final String DELETE_SUCCESS = "Deleted successfully";

    // Validation Messages
    public static final String NAME_REQUIRED = "Name is required";
    public static final String BASE_PRICE_REQUIRED = "Base price is required";
    public static final String BASE_PRICE_POSITIVE = "Base price must be positive or zero";
    public static final String TYPE_REQUIRED = "Type is required";
    public static final String UNIT_PRICE_REQUIRED = "Unit price is required";
    public static final String UNIT_PRICE_POSITIVE = "Unit price must be positive or zero";
    public static final String HEX_CODE_REQUIRED = "HEX code is required";
    public static final String INVALID_HEX_CODE = "Invalid HEX code format";
}
