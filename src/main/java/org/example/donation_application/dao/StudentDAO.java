package org.example.donation_application.dao;
import org.example.donation_application.models.Student;
import org.example.donation_application.utils.PasswordUtils;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {
    public boolean registerStudent(Student student) {
        String sql = "INSERT INTO pending_users (name, email, password, phone, address, school, stream, reason_for_registration, student_picture, need_amount) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, student.getName());
            stmt.setString(2, student.getEmail());
            stmt.setString(3, student.getPassword());
            stmt.setString(4, student.getPhone());
            stmt.setString(5, student.getAddress());
            stmt.setString(6, student.getSchool());
            stmt.setString(7, student.getStream().getValue());
            stmt.setString(8, student.getReasonForRegistration());

            if (student.getStudentPicture() != null) {
                stmt.setBytes(9, student.getStudentPicture());
            } else {
                stmt.setNull(9, Types.LONGVARBINARY);
            }

            // FIXED: Safe handling of needAmount
            if (student.getNeedAmount() != null) {
                stmt.setInt(10, student.getNeedAmount());
            } else {
                stmt.setInt(10, 0); // Default to 0 if null
            }

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("SQL Error in registerStudent: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    public List<Student> getPendingStudents() {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM pending_users ORDER BY created_at ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Student student = new Student();
                student.setId(rs.getInt("id"));
                student.setName(rs.getString("name"));
                student.setEmail(rs.getString("email"));
                student.setPhone(rs.getString("phone"));
                student.setAddress(rs.getString("address"));
                student.setSchool(rs.getString("school"));
                student.setStream(Student.Stream.fromString(rs.getString("stream")));
                student.setReasonForRegistration(rs.getString("reason_for_registration"));
                student.setNeedAmount(rs.getInt("need_amount"));
                byte[] pictureBytes = rs.getBytes("student_picture");
                if (pictureBytes != null) {
                    student.setStudentPicture(pictureBytes);
                }

                student.setPending(true);
                students.add(student);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return students;
    }

    public boolean approveStudent(int pendingId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            // Get student data from pending_users
            String selectSql = "SELECT * FROM pending_users WHERE id = ?";
            PreparedStatement selectStmt = conn.prepareStatement(selectSql);
            selectStmt.setInt(1, pendingId);
            ResultSet rs = selectStmt.executeQuery();
            if (rs.next()) {
                // Insert into students table
                // Inside the approveStudent method, replace the insertion part:
                String insertSql = "INSERT INTO students (name, email, password, phone, address, school, stream, reason_for_registration, student_picture, need_amount) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                insertStmt.setString(1, rs.getString("name"));
                insertStmt.setString(2, rs.getString("email"));
                insertStmt.setString(3, rs.getString("password"));
                insertStmt.setString(4, rs.getString("phone"));
                insertStmt.setString(5, rs.getString("address"));
                insertStmt.setString(6, rs.getString("school"));
                insertStmt.setString(7, rs.getString("stream"));
                insertStmt.setString(8, rs.getString("reason_for_registration"));
                byte[] pictureBytes = rs.getBytes("student_picture");
                if (pictureBytes != null) {
                    insertStmt.setBytes(9, pictureBytes);  // Parameter 9 for picture
                } else {
                    insertStmt.setNull(9, Types.LONGVARBINARY);
                }
                insertStmt.setInt(10, rs.getInt("need_amount")); // Parameter 10 for need_amount


                insertStmt.executeUpdate();
                // Delete from pending_users
                String deleteSql = "DELETE FROM pending_users WHERE id = ?";
                PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
                deleteStmt.setInt(1, pendingId);
                deleteStmt.executeUpdate();
                conn.commit();
                return true;
            }
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public boolean rejectStudent(int pendingId) {
        String sql = "DELETE FROM pending_users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pendingId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Student> getAllStudents() {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM students ORDER BY name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Student student = new Student();
                student.setId(rs.getInt("id"));
                student.setName(rs.getString("name"));
                student.setEmail(rs.getString("email"));
                student.setPhone(rs.getString("phone"));
                student.setAddress(rs.getString("address"));
                student.setSchool(rs.getString("school"));
                student.setStream(Student.Stream.fromString(rs.getString("stream")));
                student.setReasonForRegistration(rs.getString("reason_for_registration"));
                student.setNeedAmount(rs.getInt("need_amount"));
                byte[] pictureBytes = rs.getBytes("student_picture");
                if (pictureBytes != null) {
                    student.setStudentPicture(pictureBytes);
                }

                student.setPending(false);
                students.add(student);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return students;
    }

    public boolean removeStudent(int studentId) {
        String sql = "DELETE FROM students WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public List<Student> getStudentsByStream(Student.Stream stream) {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM students WHERE stream = ? ORDER BY name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, stream.getValue());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Student student = new Student();
                    student.setId(rs.getInt("id"));
                    student.setName(rs.getString("name"));
                    student.setEmail(rs.getString("email"));
                    student.setPhone(rs.getString("phone"));
                    student.setAddress(rs.getString("address"));
                    student.setSchool(rs.getString("school"));
                    student.setStream(Student.Stream.fromString(rs.getString("stream")));
                    student.setReasonForRegistration(rs.getString("reason_for_registration"));
                    student.setNeedAmount(rs.getInt("need_amount"));
                    byte[] pictureBytes = rs.getBytes("student_picture");
                    if (pictureBytes != null) {
                        student.setStudentPicture(pictureBytes);
                    }

                    student.setPending(false);
                    students.add(student);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return students;
    }

}
