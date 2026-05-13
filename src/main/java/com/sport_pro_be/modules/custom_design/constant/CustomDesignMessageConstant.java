package com.sport_pro_be.modules.custom_design.constant;

public final class CustomDesignMessageConstant {

    private CustomDesignMessageConstant() {}

    // Success
    public static final String DESIGN_SAVED_SUCCESS = "Design saved successfully.";
    public static final String DESIGN_DELETED_SUCCESS = "Design deleted successfully.";

    // Not Found
    public static final String DESIGN_NOT_FOUND = "Custom design not found.";
    public static final String MATERIAL_NOT_FOUND = "Printing material not found.";
    public static final String PRICE_CONFIG_NOT_FOUND = "Printing price config for type '%s' is not configured.";

    // Validation
    public static final String FILE_REQUIRED = "Design image file is required.";
    public static final String MATERIAL_ID_REQUIRED = "Material ID is required.";
    public static final String NUM_TEXT_LINES_REQUIRED = "Number of text lines is required.";
    public static final String NUM_IMAGES_REQUIRED = "Number of images is required.";
    public static final String NUM_TEXT_LINES_MIN = "Number of text lines must be at least 0.";
    public static final String NUM_IMAGES_MIN = "Number of images must be at least 0.";

    // Security
    public static final String DESIGN_ACCESS_DENIED = "You do not have permission to access this design.";

    // Upload
    public static final String UPLOAD_FOLDER = "custom_designs";
}
