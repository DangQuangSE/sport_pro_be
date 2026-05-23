package com.sport_pro_be.modules.color.constant;

public final class ColorMessageConstant {
    private ColorMessageConstant() {
    }

    public static final String COLOR_NOT_FOUND = "Color not found";
    public static final String COLOR_NAME_EXISTS = "Color name already exists";
    public static final String COLOR_CREATED = "Color created successfully";
    public static final String COLOR_UPDATED = "Color updated successfully";
    public static final String COLOR_DELETED = "Color deleted successfully";
    public static final String COLORS_RETRIEVED = "Colors retrieved successfully";

    // Validation messages
    public static final String COLOR_NAME_REQUIRED = "Color name is required";
    public static final String COLOR_HEX_REQUIRED = "Color hex code is required";
    public static final String COLOR_HEX_INVALID = "Invalid hexadecimal color code";
}
