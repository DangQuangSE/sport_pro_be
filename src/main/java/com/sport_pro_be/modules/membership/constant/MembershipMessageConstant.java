package com.sport_pro_be.modules.membership.constant;

public class MembershipMessageConstant {
    // Error Messages
    public static final String TIER_CONFIG_NOT_FOUND = "Tier configuration not found";

    // Success Messages
    public static final String TIER_CONFIGS_RETRIEVED = "Tier configurations retrieved successfully";
    public static final String TIER_CONFIG_UPDATED = "Tier configuration updated successfully";
    public static final String TIER_CREATED = "Membership tier created successfully";
    public static final String TIER_UPDATED = "Membership tier updated successfully";
    public static final String TIER_DELETED = "Membership tier deleted successfully";
    public static final String TIERS_RETRIEVED = "Membership tiers retrieved successfully";
    public static final String TIER_CODE_INVALID = "Tier code must contain only letters, numbers, and underscores";
    public static final String TIER_VERSION_CONFLICT = "Membership tier was changed by another administrator";
    public static final String TIER_DEPENDENCY_CONFLICT = "Membership tier is referenced and cannot be deleted";
    public static final String TIER_INVALID_CONFIGURATION = "Membership tier configuration is invalid";
}
