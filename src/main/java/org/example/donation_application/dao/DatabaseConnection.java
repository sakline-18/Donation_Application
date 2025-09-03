//package org.example.donation_application.dao;
//
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.SQLException;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//
//public class DatabaseConnection {
//    private static final String URL = "jdbc:mysql://localhost:3306/donation_portal";
//    private static final String USERNAME = "root"; // Update with your credentials
//    private static final String PASSWORD = "1234"; // Update with your password
//
//    static {
//        try {
//            Class.forName("com.mysql.cj.jdbc.Driver");
//            System.out.println("MySQL JDBC Driver loaded successfully");
//        } catch (ClassNotFoundException e) {
//            System.err.println("MySQL JDBC Driver not found");
//            throw new RuntimeException("MySQL JDBC Driver not found", e);
//        }
//    }
//
//    public static Connection getConnection() throws SQLException {
//        try {
//            Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
//            System.out.println("Database connection successful");
//            return conn;
//        } catch (SQLException e) {
//            System.err.println("Database connection failed: " + e.getMessage());
//            throw e;
//        }
//    }
//
//    // Add this method to test moderator exists
//    public static void testModeratorExists() {
//        String sql = "SELECT * FROM moderators WHERE email = ?";
//        try (Connection conn = getConnection();
//             PreparedStatement stmt = conn.prepareStatement(sql)) {
//
//            stmt.setString(1, "admin@portal.com");
//            ResultSet rs = stmt.executeQuery();
//
//            if (rs.next()) {
//                System.out.println("✓ Moderator found:");
//                System.out.println("  ID: " + rs.getInt("id"));
//                System.out.println("  Name: " + rs.getString("name"));
//                System.out.println("  Email: " + rs.getString("email"));
//                System.out.println("  Password hash: " + rs.getString("password"));
//            } else {
//                System.err.println("✗ No moderator found with email: admin@portal.com");
//                System.err.println("Run this SQL to create moderator:");
//                System.err.println("INSERT INTO moderators (name, email, password) VALUES " +
//                        "('Admin', 'admin@portal.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi');");
//            }
//        } catch (SQLException e) {
//            System.err.println("Database test failed: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//}

package org.example.donation_application.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/donation_portal";
    private static final String USERNAME = "root"; // Update with your credentials
    private static final String PASSWORD = "1234"; // Update with your password

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✓ MySQL JDBC Driver loaded successfully");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ MySQL JDBC Driver not found");
            throw new RuntimeException("MySQL JDBC Driver not found", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            System.out.println("Attempting database connection...");
            System.out.println("URL: " + URL);
            System.out.println("Username: " + USERNAME);
            Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("✓ Database connection successful");
            return conn;
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed: " + e.getMessage());
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("SQL State: " + e.getSQLState());
            throw e;
        }
    }

    // Test method to verify moderator exists
    public static void testModeratorExists() {
        System.out.println("\n=== TESTING MODERATOR IN DATABASE ===");
        String sql = "SELECT * FROM moderators";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println("Moderator found:");
                System.out.println("  ID: " + rs.getInt("id"));
                System.out.println("  Name: '" + rs.getString("name") + "'");
                System.out.println("  Email: '" + rs.getString("email") + "'");
                System.out.println("  Password: '" + rs.getString("password") + "'");
                System.out.println("  Created: " + rs.getTimestamp("created_at"));
            }

            if (!found) {
                System.err.println("❌ No moderators found in database!");
                System.err.println("Run this SQL to create moderator:");
                System.err.println("INSERT INTO moderators (name, email, password) VALUES ('Admin', 'admin@portal.com', 'admin123');");
            }
        } catch (SQLException e) {
            System.err.println("❌ Database test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
