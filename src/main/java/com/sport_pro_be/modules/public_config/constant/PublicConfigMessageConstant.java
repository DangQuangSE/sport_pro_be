package com.sport_pro_be.modules.public_config.constant;

public class PublicConfigMessageConstant {
    private PublicConfigMessageConstant() {}
    public static final String CONFIGS_RETRIEVED = "Configurations retrieved successfully";
    public static final String CONFIG_CREATED = "Configuration created successfully";
    public static final String CONFIG_UPDATED = "Configuration updated successfully";
    public static final String CONFIG_DELETED = "Configuration deleted successfully";
    public static final String CONFIG_NOT_FOUND = "Configuration key not found";
    public static final String CONFIG_ALREADY_EXISTS = "Configuration key already exists";
    public static final String CONFIG_VERSION_CONFLICT = "Configuration was changed by another administrator";
    public static final String CONFIG_REQUIRED = "This configuration is required by the pricing engine";
    public static final String CONFIG_INVALID_VALUE = "Configuration value is invalid";
    public static final String CONFIG_SECRET_NOT_ALLOWED = "Infrastructure secrets must remain in secret storage";
    public static final String CONFIG_PUBLIC_KEY_NOT_ALLOWED = "Only approved content settings may use PUBLIC scope";
    public static final String CONFIG_METADATA_MISMATCH = "Configuration metadata does not match its server definition";
}
