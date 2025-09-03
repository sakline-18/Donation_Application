package org.example.donation_application.utils;

import java.util.regex.Pattern;

public class ValidationUtils {
    private static final String EMAIL_PATTERN =
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);

    public static boolean isValidEmail(String email) {
        return email != null && pattern.matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    public static boolean isValidName(String name) {
        return name != null && name.trim().length() >= 2;
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && phone.matches("\\d{10,15}");
    }

    public static boolean isEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }
}
