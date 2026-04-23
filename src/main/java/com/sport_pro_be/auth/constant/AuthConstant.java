package com.sport_pro_be.auth.constant;

public class AuthConstant {
    private AuthConstant() {
    }

    public static final String EMAIL_EXIST = "Email already exists";
    public static final String INVALID_CREDENTIALS = "Invalid email or password";
    public static final String EMAIL_NOT_VERIFIED = "Email is not verified. Please verify OTP first";
    public static final String INVALID_OTP = "Invalid OTP";
    public static final String OTP_INCORRECT = "OTP code is incorrect";
    public static final String OTP_EXPIRED = "OTP has expired";
    public static final String OTP_LOCKED_TOO_MANY_ATTEMPTS = "OTP has been locked due to too many incorrect attempts";
    public static final String OTP_REQUEST_TOO_FREQUENT = "You are requesting OTP too frequently. Please try again in a few seconds";
    public static final String ACCOUNT_NOT_FOUND = "Account not found";
    public static final String ACCOUNT_ALREADY_VERIFIED = "Account is already verified";
    public static final int OTP_MAX_ATTEMPTS = 5;
}
