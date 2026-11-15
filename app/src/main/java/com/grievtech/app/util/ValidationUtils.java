package com.grievtech.app.util;

import java.util.regex.Pattern;

/**
 * Utility class for validating input data in GrievTech.
 */
public final class ValidationUtils {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[+]?[0-9]{10,14}$"
    );

    private ValidationUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Validates if the given full name is valid (at least 2 characters).
     *
     * @param fullName Full name string to validate
     * @return true if valid, false otherwise
     */
    public static boolean validateFullName(String fullName) {
        if (fullName == null) {
            return false;
        }
        String trimmed = fullName.trim();
        return trimmed.length() >= 2;
    }

    /**
     * Validates if the given email address has a valid format.
     *
     * @param email Email string to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validates if the given phone number is valid.
     *
     * @param phone Phone string to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone.trim().replaceAll("\\s+", "")).matches();
    }

    /**
     * Validates if password and confirm password match.
     *
     * @param password Original password
     * @param confirmPassword Confirm password to compare
     * @return true if matching and non-empty, false otherwise
     */
    public static boolean validateConfirmPassword(String password, String confirmPassword) {
        if (password == null || confirmPassword == null) {
            return false;
        }
        return !password.isEmpty() && password.equals(confirmPassword);
    }

    /**
     * Checks password strength.
     * Password must be at least 8 characters long, contain a digit, an uppercase letter, and a special character for STRONG.
     *
     * @param password Password to evaluate
     * @return PasswordStrength enum value
     */
    public static PasswordStrength checkPasswordStrength(String password) {
        if (password == null || password.length() < 6) {
            return PasswordStrength.WEAK;
        }
        boolean hasDigit = false;
        boolean hasUpper = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isDigit(c)) hasDigit = true;
            else if (Character.isUpperCase(c)) hasUpper = true;
            else if (!Character.isLetterOrDigit(c)) hasSpecial = true;
        }

        if (password.length() >= 8 && hasDigit && hasUpper && hasSpecial) {
            return PasswordStrength.STRONG;
        } else if (hasDigit || hasUpper) {
            return PasswordStrength.MEDIUM;
        }
        return PasswordStrength.WEAK;
    }

    public enum PasswordStrength {
        WEAK,
        MEDIUM,
        STRONG
    }
}
