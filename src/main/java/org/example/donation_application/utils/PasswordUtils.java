package org.example.donation_application.utils;

public class PasswordUtils {
    // No BCrypt - just return password as-is (INSECURE!)
    public static String hashPassword(String plainPassword) {
        return plainPassword; // Store as plaintext
    }

    public static boolean checkPassword(String plainPassword, String storedPassword) {
        return plainPassword.equals(storedPassword); // Direct comparison
    }
}
