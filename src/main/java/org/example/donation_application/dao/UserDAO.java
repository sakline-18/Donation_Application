package org.example.donation_application.dao;

import org.example.donation_application.models.Donor;
import org.example.donation_application.models.Moderator;
import org.example.donation_application.models.Student;
import org.example.donation_application.models.User;

import java.sql.*;

public class UserDAO {

    public boolean isEmailExists(String email) {
        System.out.println("\n=== isEmailExists('" + email + "') ===");
        String[] tables = {"students", "donors", "moderators", "pending_users"};
        for (String t : tables) {
            String sql = "SELECT COUNT(*) FROM " + t + " WHERE email = ?";
            try (Connection c = DatabaseConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, email.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        System.out.println("Email found in " + t);
                        return true;
                    } else {
                        System.out.println("Email not in " + t);
                    }
                }
            } catch (SQLException e) {
                System.err.println("isEmailExists error on " + t + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        return false;
    }

    public User authenticateUser(String email, String password) {
        System.out.println("\n========== AUTHENTICATE USER ==========");
        System.out.println("Email='" + email + "', Password(len)=" + (password == null ? -1 : password.length()));

        // 1) Moderators first
        User u = authenticateFromTable(email, password, "moderators", "moderator");
        if (u != null) { System.out.println("Authenticated as MODERATOR"); return u; }

        // 2) Donors
        u = authenticateFromTable(email, password, "donors", "donor");
        if (u != null) { System.out.println("Authenticated as DONOR"); return u; }

        // 3) Students
        u = authenticateFromTable(email, password, "students", "student");
        if (u != null) { System.out.println("Authenticated as STUDENT"); return u; }

        // 4) Pending students
        u = authenticateFromTable(email, password, "pending_users", "pending");
        if (u != null) { System.out.println("Authenticated as PENDING student"); return u; }

        System.err.println("Auth failed across all tables");
        return null;
    }

    private User authenticateFromTable(String email, String password, String table, String type) {
        String sql = "SELECT * FROM " + table + " WHERE email = ?";
        System.out.println("\n-- Querying table: " + table + " | SQL: " + sql);
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("No row in " + table + " for email='" + email + "'. Counting rows...");
                    try (PreparedStatement cnt = c.prepareStatement("SELECT COUNT(*) AS n FROM " + table);
                         ResultSet crs = cnt.executeQuery()) {
                        if (crs.next()) System.out.println(table + " total rows=" + crs.getInt("n"));
                    }
                    return null;
                }

                // Basic diagnostics
                String dbEmail = safeGet(rs, "email");
                String dbPass  = safeGet(rs, "password");
                System.out.println("Row -> email='" + dbEmail + "', passLen=" + (dbPass == null ? -1 : dbPass.length()));

                // Plain equals comparison (hashing removed)
                boolean eq = password != null && password.equals(dbPass);
                System.out.println("Plain equals => " + eq);
                if (!eq && password != null && dbPass != null) {
                    System.out.println("Trimmed equals => " + password.trim().equals(dbPass.trim()));
                }
                if (!(password != null && dbPass != null && password.equals(dbPass))) return null;

                // Build and return the user for this table
                try {
                    return createUserFromResultSet(rs, type);
                } catch (SQLException e) {
                    System.err.println("User build failed for " + type + ": " + e.getMessage());
                    e.printStackTrace();
                    return null;
                }
            }
        } catch (SQLException e) {
            System.err.println("DB error in table " + table + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Safe getter: returns null if column missing
    private String safeGet(ResultSet rs, String column) {
        try {
            return hasColumn(rs, column) ? rs.getString(column) : null;
        } catch (SQLException e) {
            System.err.println("safeGet error for column '" + column + "': " + e.getMessage());
            return null;
        }
    }

    // Check whether a column exists in the current ResultSet
    private boolean hasColumn(ResultSet rs, String column) {
        try {
            ResultSetMetaData md = rs.getMetaData();
            int count = md.getColumnCount();
            for (int i = 1; i <= count; i++) {
                String label = md.getColumnLabel(i);
                if (label != null && label.equalsIgnoreCase(column)) return true;
                String name  = md.getColumnName(i);
                if (name  != null && name.equalsIgnoreCase(column))  return true;
            }
        } catch (SQLException e) {
            System.err.println("hasColumn error: " + e.getMessage());
        }
        return false;
    }

    // Build user object safely per table schema
    private User createUserFromResultSet(ResultSet rs, String userType) throws SQLException {
        switch (userType) {
            case "moderator": {
                Moderator m = new Moderator();
                m.setId(rs.getInt("id"));
                if (hasColumn(rs, "name"))     m.setName(rs.getString("name"));
                if (hasColumn(rs, "email"))    m.setEmail(rs.getString("email"));
                if (hasColumn(rs, "password")) m.setPassword(rs.getString("password"));
                // moderators table has no 'phone' column by schema; do not read it
                return m;
            }
            case "donor": {
                Donor d = new Donor();
                d.setId(rs.getInt("id"));
                if (hasColumn(rs, "name"))         d.setName(rs.getString("name"));
                if (hasColumn(rs, "email"))        d.setEmail(rs.getString("email"));
                if (hasColumn(rs, "password"))     d.setPassword(rs.getString("password"));
                if (hasColumn(rs, "phone"))        d.setPhone(rs.getString("phone"));
                if (hasColumn(rs, "organization")) d.setOrganization(rs.getString("organization"));
                return d;
            }
            case "student": {
                Student s = new Student();
                s.setId(rs.getInt("id"));
                if (hasColumn(rs, "name"))           s.setName(rs.getString("name"));
                if (hasColumn(rs, "email"))          s.setEmail(rs.getString("email"));
                if (hasColumn(rs, "password"))       s.setPassword(rs.getString("password"));
                if (hasColumn(rs, "phone"))          s.setPhone(rs.getString("phone"));
                if (hasColumn(rs, "address"))        s.setAddress(rs.getString("address"));
                if (hasColumn(rs, "school"))         s.setSchool(rs.getString("school"));
                if (hasColumn(rs, "stream"))         s.setStream(Student.Stream.fromString(rs.getString("stream")));
                if (hasColumn(rs, "reason_for_registration")) s.setReasonForRegistration(rs.getString("reason_for_registration"));
                if (hasColumn(rs, "student_picture")) s.setStudentPicture(rs.getBytes("student_picture"));
                s.setPending(false);
                return s;
            }

            case "pending": {
                Student s = new Student();
                s.setId(rs.getInt("id"));
                if (hasColumn(rs, "name"))           s.setName(rs.getString("name"));
                if (hasColumn(rs, "email"))          s.setEmail(rs.getString("email"));
                if (hasColumn(rs, "password"))       s.setPassword(rs.getString("password"));
                if (hasColumn(rs, "phone"))          s.setPhone(rs.getString("phone"));
                if (hasColumn(rs, "address"))        s.setAddress(rs.getString("address"));
                if (hasColumn(rs, "school"))         s.setSchool(rs.getString("school"));
                if (hasColumn(rs, "stream"))         s.setStream(Student.Stream.fromString(rs.getString("stream")));
                if (hasColumn(rs, "reason_for_registration")) s.setReasonForRegistration(rs.getString("reason_for_registration"));
                if (hasColumn(rs, "student_picture")) s.setStudentPicture(rs.getBytes("student_picture"));
                s.setPending(true);
                return s;
            }

            default:
                return null;
        }
    }
}
