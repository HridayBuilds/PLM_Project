package com.odoo.plm.util;

public class Constants {

    // App Info
    public static final String APP_NAME = "Ecova";

    // Token Expiration
    public static final int VERIFICATION_TOKEN_EXPIRATION_HOURS = 24;
    public static final int PASSWORD_RESET_TOKEN_EXPIRATION_HOURS = 1;

    // Email Templates
    public static final String VERIFICATION_EMAIL_SUBJECT = "Verify Your Email - Ecova";
    public static final String PASSWORD_RESET_EMAIL_SUBJECT = "Reset Your Password - Ecova";

    // Validation Messages
    public static final String INVALID_TOKEN = "Invalid or expired token";
    public static final String TOKEN_EXPIRED = "Token has expired";
    public static final String TOKEN_ALREADY_USED = "Token has already been used";
    public static final String USER_NOT_FOUND = "User not found";
    public static final String LOGIN_ID_EXISTS = "Login ID already exists";
    public static final String EMAIL_EXISTS = "Email already exists";
    public static final String INVALID_CREDENTIALS = "Invalid login ID or password";
    public static final String ACCOUNT_NOT_VERIFIED = "Account is not verified. Please check your email.";
    public static final String ACCOUNT_DISABLED = "Account has been disabled. Please contact support.";
    public static final String PASSWORD_MISMATCH = "Current password is incorrect";
    public static final String PASSWORDS_DO_NOT_MATCH = "Password and confirm password do not match";
    public static final String INVALID_LOGIN_ID_FORMAT = "Login ID must start with 'ECV' and be 6-12 characters total";

    // Success Messages
    public static final String SIGNUP_SUCCESS = "Registration successful! Please check your email to verify your account.";
    public static final String LOGIN_SUCCESS = "Login successful";
    public static final String VERIFICATION_SUCCESS = "Email verified successfully! You can now login.";
    public static final String PASSWORD_RESET_EMAIL_SENT = "Password reset instructions have been sent to your email.";
    public static final String PASSWORD_RESET_SUCCESS = "Password has been reset successfully.";
    public static final String PASSWORD_CHANGE_SUCCESS = "Password changed successfully.";
    public static final String PROFILE_UPDATE_SUCCESS = "Profile updated successfully.";

    private Constants() {
        // Private constructor to prevent instantiation
    }
}
