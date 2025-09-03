package org.example.donation_application.dao;

import org.example.donation_application.models.Donation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DonationDAO {

    public boolean createDonation(Donation donation) {
        String sql = "INSERT INTO donations (donor_id, student_id, amount, description) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, donation.getDonorId());
            stmt.setInt(2, donation.getStudentId());
            stmt.setBigDecimal(3, donation.getAmount());
            stmt.setString(4, donation.getDescription());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Donation> getDonationsByDonor(int donorId) {
        List<Donation> donations = new ArrayList<>();
        String sql = "SELECT d.*, s.name as student_name FROM donations d " +
                "JOIN students s ON d.student_id = s.id " +
                "WHERE d.donor_id = ? ORDER BY d.donation_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, donorId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Donation donation = new Donation();
                donation.setId(rs.getInt("id"));
                donation.setDonorId(rs.getInt("donor_id"));
                donation.setStudentId(rs.getInt("student_id"));
                donation.setAmount(rs.getBigDecimal("amount"));
                donation.setDescription(rs.getString("description"));
                donation.setDonationDate(rs.getTimestamp("donation_date").toLocalDateTime());
                donation.setStudentName(rs.getString("student_name"));
                donations.add(donation);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return donations;
    }

    public List<Donation> getDonationsByStudent(int studentId) {
        List<Donation> donations = new ArrayList<>();
        String sql = "SELECT d.*, donors.name as donor_name FROM donations d " +
                "JOIN donors ON d.donor_id = donors.id " +
                "WHERE d.student_id = ? ORDER BY d.donation_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Donation donation = new Donation();
                donation.setId(rs.getInt("id"));
                donation.setDonorId(rs.getInt("donor_id"));
                donation.setStudentId(rs.getInt("student_id"));
                donation.setAmount(rs.getBigDecimal("amount"));
                donation.setDescription(rs.getString("description"));
                donation.setDonationDate(rs.getTimestamp("donation_date").toLocalDateTime());
                donation.setDonorName(rs.getString("donor_name"));
                donations.add(donation);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return donations;
    }
    /**
     * Get all donations with donor and student names
     */
    public List<Donation> getAllDonationsWithNames() {
        List<Donation> donations = new ArrayList<>();
        String sql = """
        SELECT d.*, 
               donors.name as donor_name, 
               students.name as student_name 
        FROM donations d
        LEFT JOIN donors ON d.donor_id = donors.id
        LEFT JOIN students ON d.student_id = students.id
        ORDER BY d.donation_date DESC
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Donation donation = new Donation();
                donation.setId(rs.getInt("id"));
                donation.setDonorId(rs.getInt("donor_id"));
                donation.setStudentId(rs.getInt("student_id"));
                donation.setAmount(rs.getBigDecimal("amount"));
                donation.setDescription(rs.getString("description"));
                donation.setDonationDate(rs.getTimestamp("donation_date").toLocalDateTime());
                donation.setDonorName(rs.getString("donor_name"));
                donation.setStudentName(rs.getString("student_name"));
                donations.add(donation);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all donations: " + e.getMessage());
            e.printStackTrace();
        }

        return donations;
    }

}
