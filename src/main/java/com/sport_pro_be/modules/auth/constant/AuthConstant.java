package com.sport_pro_be.modules.auth.constant;

public class AuthConstant {
    private AuthConstant() {
    }

    public static final String EMAIL_EXIST = "Email already exists";
    public static final String OTP_SENT_SUCCESS = "OTP has been sent to your email";
    public static final String OTP_RESENT_SUCCESS = "OTP has been resent to your email";
    public static final String OTP_VERIFIED_FOR_REGISTRATION = "OTP verified successfully. You can now complete registration";
    public static final String REGISTRATION_SUCCESS = "Registration successful. You can now login";
    public static final String LOGOUT_SUCCESS = "Logged out successfully";
    public static final String INVALID_CREDENTIALS = "Invalid email or password";
    public static final String EMAIL_NOT_VERIFIED = "Email is not verified. Please verify OTP first";
    public static final String OTP_VERIFICATION_REQUIRED = "Please verify OTP for this email before registering";
    public static final String INVALID_OTP = "Invalid OTP";
    public static final String OTP_INCORRECT = "OTP code is incorrect";
    public static final String OTP_EXPIRED = "OTP has expired";
    public static final String OTP_LOCKED_TOO_MANY_ATTEMPTS = "OTP has been locked due to too many incorrect attempts";
    public static final String OTP_REQUEST_TOO_FREQUENT = "You are requesting OTP too frequently. Please try again in a few seconds";
    public static final String ACCOUNT_NOT_FOUND = "Account not found";
    public static final String ACCOUNT_ALREADY_VERIFIED = "Account is already verified";
    public static final String REFRESH_TOKEN_REQUIRED = "Refresh token is required";
    public static final String REFRESH_TOKEN_INVALID_OR_EXPIRED = "Refresh token is invalid or expired";
    public static final String REFRESH_TOKEN_REVOKED = "Refresh token has been revoked";
    public static final String REFRESH_TOKEN_REUSE_DETECTED = "Refresh token reuse detected. Please login again";
    public static final String APP_JWT_SECRET_INVALID = "APP_JWT_SECRET must be at least 32 characters long";
    
    // Forgot Password
    public static final String FORGOT_PASSWORD_OTP_SENT = "If your email exists in our system, an OTP has been sent";
    public static final String INVALID_FORGOT_PASSWORD_TOKEN = "Invalid or expired forgot password token";
    public static final String PASSWORD_RESET_SUCCESS = "Password has been reset successfully. Please login with your new password";
    
    // Auth Success
    public static final String LOGIN_SUCCESS = "Login successful";
    public static final String TOKEN_REFRESHED = "Token refreshed successfully";
    public static final String USER_DETAILS_RETRIEVED = "User details retrieved successfully";
    public static final String OTP_VERIFIED_SUCCESS = "OTP verified successfully";

    // Validation Messages
    public static final String EMAIL_REQUIRED = "Email is required";
    public static final String EMAIL_INVALID = "Email is invalid";
    public static final String PASSWORD_REQUIRED = "Password is required";
    public static final String PASSWORD_MIN_SIZE = "Password must be at least 6 characters";
    public static final String OTP_CODE_REQUIRED = "OTP code is required";
    public static final String OTP_INVALID_FORMAT = "OTP must be exactly 6 digits";
    public static final String TOKEN_REQUIRED = "Token is required";

    public static final int OTP_MAX_ATTEMPTS = 5;
    public static final String SHA_256_NOT_AVAILABLE = "SHA-256 algorithm is not available";
}

