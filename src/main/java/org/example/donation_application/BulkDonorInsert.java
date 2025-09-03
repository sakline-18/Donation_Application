package org.example.donation_application;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BulkDonorInsert {

    // Update these with your database credentials
    private static final String DB_URL = "jdbc:mysql://localhost:3306/donation_portal";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "1234";

    private static final String[] NAMES = {
            "Mohammad Rahman", "Abdul Karim", "Shahidul Islam", "Mizanur Rahman", "Aminul Haque",
            "Motiur Rahman", "Ruhul Amin", "Fazlur Rahman", "Harun-or-Rashid", "Abdur Rouf",
            "Kamrul Hassan", "Shahjahan Ali", "Mahbubur Rahman", "Delwar Hossain", "Mosharraf Hossain",
            "Jahangir Alam", "Nurul Islam", "Rafiqul Islam", "Shamsul Huda", "Golam Mostafa",
            "Abul Kalam", "Sirajul Islam", "Faruk Ahmed", "Rezaul Karim", "Nazrul Islam",
            "Akram Hossain", "Babul Ahmed", "Khalilur Rahman", "Iqbal Hossain", "Zakir Hossain"
    };

    private static final String[] ORGANIZATIONS = {
            "Grameen Bank", "BRAC", "City Bank Limited", "Dhaka Bank", "Trust Bank",
            "ASA NGO", "Proshika", "Square Pharmaceuticals", "Beximco Group", "ACI Limited",
            "Bashundhara Group", "Summit Group", "Meghna Group", "TK Group", "DBL Group",
            "Akij Group", "Pran-RFL Group", "Fresh Group", "PHP Group", "Ha-Meem Group",
            "Personal", "Self Employed", "Mutual Trust Bank", "Prime Bank", "Southeast Bank",
            "Janata Bank", "Sonali Bank", "Agrani Bank", "Rupali Bank", "Bangladesh Bank"
    };

    public static void main(String[] args) {
        System.out.println("Starting individual donor inserts...");

        String sql = "INSERT INTO donors (name, email, password, phone, organization, donor_picture, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, NOW())";

        int successCount = 0;
        int errorCount = 0;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {

            for (int i = 1; i <= 25; i++) {
                if(i==15){
                    continue;
                }
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

                    String name = NAMES[i - 1];
                    String email = generateEmail(name, i);
                    String hashedPassword = hashPassword("donor123");
                    String phone = generatePhone(i);
                    String organization = ORGANIZATIONS[i - 1];
                    String imagePath = String.format("C:\\Users\\User\\Desktop\\pics\\donor-pics\\donor-pic-%d.jpeg", i);

                    // Set basic parameters
                    pstmt.setString(1, name);
                    pstmt.setString(2, email);
                    pstmt.setString(3, hashedPassword);
                    pstmt.setString(4, phone);
                    pstmt.setString(5, organization);

                    // Handle image insertion
                    try (FileInputStream fis = new FileInputStream(imagePath)) {
                        pstmt.setBinaryStream(6, fis, fis.available());

                        // Execute immediately while InputStream is open
                        int rowsAffected = pstmt.executeUpdate();

                        if (rowsAffected > 0) {
                            successCount++;
                            System.out.println("✅ Inserted donor " + i + ": " + name);
                        }

                    } catch (IOException e) {
                        System.err.println("❌ Error reading image for donor " + i + ": " + e.getMessage());
                        // Insert without image
                        pstmt.setNull(6, java.sql.Types.LONGVARBINARY);
                        int rowsAffected = pstmt.executeUpdate();
                        if (rowsAffected > 0) {
                            successCount++;
                            System.out.println("⚠️ Inserted donor " + i + " without image: " + name);
                        }
                    }

                } catch (SQLException e) {
                    errorCount++;
                    System.err.println("❌ Database error for donor " + i + ": " + e.getMessage());
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Connection error: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        System.out.println("\n=== INSERT SUMMARY ===");
        System.out.println("✅ Successful inserts: " + successCount);
        System.out.println("❌ Failed inserts: " + errorCount);
        System.out.println("📊 Total processed: " + (successCount + errorCount));
    }

    private static String generateEmail(String name, int index) {
        String emailName = name.toLowerCase()
                .replace(" ", ".")
                .replace("-", "");
        return emailName + index + "@gmail.com";
    }

    private static String generatePhone(int index) {
        return String.format("01%d%07d", (index % 9) + 1, 1000000 + index);
    }

    private static String hashPassword(String password) {
        return password;
    }
}
