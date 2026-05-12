package com.sport_pro_be.modules.coupon.constant;

public class CouponMessageConstant {
    // Error Messages
    public static final String COUPON_NOT_FOUND = "Coupon not found";
    public static final String INVALID_COUPON = "Invalid or expired coupon code";
    public static final String COUPON_NOT_ACTIVE = "Coupon is not yet active";
    public static final String COUPON_EXPIRED = "Coupon has expired";
    public static final String USAGE_LIMIT_REACHED = "Coupon usage limit reached";
    public static final String MIN_AMOUNT_NOT_REACHED = "Minimum order amount not reached for this coupon";
    public static final String TIER_NOT_REACHED = "Your membership tier is not high enough to use this coupon. Required: %s";

    // Success Messages
    public static final String COUPON_CREATED = "Coupon created successfully";
    public static final String COUPON_UPDATED = "Coupon updated successfully";
    public static final String COUPON_DELETED = "Coupon deleted successfully";
    public static final String COUPONS_RETRIEVED = "Coupons retrieved successfully";
}
