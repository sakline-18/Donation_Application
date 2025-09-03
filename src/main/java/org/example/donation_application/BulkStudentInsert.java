package org.example.donation_application;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

public class BulkStudentInsert {

    // Update these with your database credentials
    private static final String DB_URL = "jdbc:mysql://localhost:3306/donation_portal";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "1234";

    // Bengali male names
    private static final String[] NAMES = {
            "Mohammad Arif Hassan", "Abdul Karim Rahman", "Shahidul Islam Khan", "Mizanur Rahman Molla", "Aminul Haque Chowdhury",
            "Motiur Rahman Sheikh", "Ruhul Amin Ahmed", "Fazlur Rahman Sarker", "Harun-or-Rashid Miah", "Abdur Rouf Talukder",
            "Kamrul Hassan Bhuiyan", "Shahjahan Ali Khan", "Mahbubur Rahman Roy", "Delwar Hossain Molla", "Mosharraf Hossain Ahmed",
            "Jahangir Alam Sheikh", "Nurul Islam Rahman", "Rafiqul Islam Khan", "Shamsul Huda Miah", "Golam Mostafa Hassan",
            "Abul Kalam Azad", "Sirajul Islam Chowdhury", "Faruk Ahmed Rahman", "Rezaul Karim Sheikh", "Nazrul Islam Hassan",
            "Akram Hossain Khan", "Babul Ahmed Molla", "Khalilur Rahman Roy", "Iqbal Hossain Ahmed", "Zakir Hossain Sheikh",
            "Tahmid Hassan Khan", "Masum Sarker Rahman", "Adnan Hossain Miah", "Salman Molla Ahmed", "Masudur Rahman Hassan"
    };

    // Bengali schools and colleges
    private static final String[] SCHOOLS = {
            "Dhaka College", "Dhaka University", "Jahangirnagar University", "BUET", "Chittagong University",
            "Rajshahi University", "Bangladesh Agricultural University", "Jagannath University", "Eden Mohila College", "Holy Cross College",
            "Notre Dame College", "Adamjee Cantonment College", "Viqarunnisa Noon College", "Government Science College", "Tejgaon College",
            "Dhanmondi Tutorial", "Shaheed Bir Uttam Lt. Boys College", "Begum Badrunnesa Government Boys College", "Government Titumir College", "Kabi Nazrul Government College",
            "Siddheswari Boys College", "Bangladesh Mohila Samity Boys High School", "Motijheel Government Boys High School", "Pogose School", "St. Gregory's High School",
            "Scholastica School", "American International School", "Maple Leaf International School", "Aga Khan School", "International School Dhaka",
            "Sunnydale School", "South Point School", "Willes Little Flower School", "Holy Cross School", "St. Francis Xavier's School"
    };

    // Stream distribution
    private static final String[] STREAMS = {"Science", "Commerce", "Arts"};

    // Realistic financial need reasons in English (for database compatibility)
    private static final String[] REASONS = {
            "My father lost his job due to the pandemic and we are struggling to pay for my tuition fees. I need financial assistance to continue my Computer Science studies.",
            "I am the eldest son in my family and need to support my education while helping my parents with household expenses. Currently facing difficulty paying semester fees.",
            "My family's small business was severely affected by recent floods. I require financial support to complete my final year in Electrical Engineering.",
            "I am from a rural area and my family depends on farming. Due to crop failure this year, we cannot afford my university expenses for Business Administration.",
            "My mother is a single parent working as a house cleaner. I need assistance to pay for my textbooks and lab fees for my Chemistry major.",
            "I am supporting my younger siblings' education while pursuing my own degree. Financial help would allow me to focus better on my Economics studies.",
            "My father is a rickshaw puller and the sole earning member of our family. I need help covering my accommodation and meal costs at university.",
            "I received a scholarship for academic excellence but it doesn't cover all expenses. Additional support needed for my Mechanical Engineering program.",
            "My family faced medical emergency expenses for my grandmother's treatment. Now struggling to pay for my English Literature course fees.",
            "I am from a low-income family in Chittagong. Need financial assistance to complete my final semester in Accounting and continue to Masters program."
    };

    // Addresses in Bangladesh
    private static final String[] ADDRESSES = {
            "Dhanmondi, Dhaka", "Uttara, Dhaka", "Gulshan, Dhaka", "Mirpur, Dhaka", "Wari, Dhaka",
            "Chittagong", "Sylhet", "Rajshahi", "Khulna", "Barisal",
            "Rangpur", "Comilla", "Narayanganj", "Gazipur", "Mymensingh",
            "Bogra", "Jessore", "Dinajpur", "Kushtia", "Pabna"
    };

    public static void main(String[] args) {
        System.out.println("Starting bulk student insert...");

        String sql = "INSERT INTO students (name, email, password, phone, address, school, stream, " +
                "reason_for_registration, student_picture, created_at, need_amount) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), ?)";

        int successCount = 0;
        int errorCount = 0;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {

            for (int i = 1; i <= 35; i++) {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

                    String name = NAMES[i - 1];
                    String email = generateEmail(name, i);
                    String hashedPassword = hashPassword("student123");
                    String phone = generatePhone(i);
                    String address = ADDRESSES[(i - 1) % ADDRESSES.length];
                    String school = SCHOOLS[(i - 1) % SCHOOLS.length];
                    String stream = STREAMS[(i - 1) % STREAMS.length];
                    String reason = REASONS[(i - 1) % REASONS.length];
                    int needAmount = generateNeedAmount(i);
                    String imagePath = String.format("C:\\Users\\User\\Desktop\\pics\\student-pics\\student-pic-%d.png", i);

                    // Set basic parameters
                    pstmt.setString(1, name);
                    pstmt.setString(2, email);
                    pstmt.setString(3, hashedPassword);
                    pstmt.setString(4, phone);
                    pstmt.setString(5, address);
                    pstmt.setString(6, school);
                    pstmt.setString(7, stream);
                    pstmt.setString(8, reason);
                    pstmt.setInt(10, needAmount);

                    // Handle image insertion
                    try (FileInputStream fis = new FileInputStream(imagePath)) {
                        pstmt.setBinaryStream(9, fis, fis.available());

                        // Execute immediately while InputStream is open
                        int rowsAffected = pstmt.executeUpdate();

                        if (rowsAffected > 0) {
                            successCount++;
                            System.out.printf("✅ Inserted student %d: %s (Stream: %s, Need: $%d)%n",
                                    i, name, stream, needAmount);
                        }

                    } catch (IOException e) {
                        System.err.printf("❌ Error reading image for student %d: %s%n", i, e.getMessage());
                        // Insert without image
                        pstmt.setNull(9, java.sql.Types.LONGVARBINARY);
                        int rowsAffected = pstmt.executeUpdate();
                        if (rowsAffected > 0) {
                            successCount++;
                            System.out.printf("⚠️ Inserted student %d without image: %s%n", i, name);
                        }
                    }

                } catch (SQLException e) {
                    errorCount++;
                    System.err.printf("❌ Database error for student %d: %s%n", i, e.getMessage());
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
        return emailName + index + "@student.edu.bd";
    }

    private static String generatePhone(int index) {
        return String.format("01%d%08d", ((index - 1) % 9) + 1, 10000000 + index);
    }

    private static int generateNeedAmount(int index) {
        // Generate realistic amounts between 15,000 to 150,000 BDT
        int[] amounts = {15000, 25000, 35000, 45000, 55000, 65000, 75000, 85000, 95000, 120000, 150000};
        return amounts[(index - 1) % amounts.length];
    }

    private static String hashPassword(String password) {
        return password;
    }
}
