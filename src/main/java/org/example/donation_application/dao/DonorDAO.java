package org.example.donation_application.dao;
import org.example.donation_application.models.Donor;
import org.example.donation_application.utils.PasswordUtils;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DonorDAO {

    public boolean registerDonor(Donor donor) {
        String sql = "INSERT INTO donors (name, email, password, phone, organization, donor_picture) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, donor.getName());
            stmt.setString(2, donor.getEmail());
            // STORE PASSWORD AS PLAINTEXT - NO HASHING
            stmt.setString(3, donor.getPassword());
            stmt.setString(4, donor.getPhone());
            stmt.setString(5, donor.getOrganization());

            if (donor.getDonorPicture() != null) {
                stmt.setBytes(6, donor.getDonorPicture());
            } else {
                stmt.setNull(6, Types.LONGVARBINARY);
            }

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Donor> getAllDonors() {
        List<Donor> donors = new ArrayList<>();
        String sql = "SELECT * FROM donors ORDER BY name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Donor donor = new Donor();
                donor.setId(rs.getInt("id"));
                donor.setName(rs.getString("name"));
                donor.setEmail(rs.getString("email"));
                donor.setPhone(rs.getString("phone"));
                donor.setOrganization(rs.getString("organization"));

                byte[] pictureBytes = rs.getBytes("donor_picture");
                if (pictureBytes != null) {
                    donor.setDonorPicture(pictureBytes);
                }

                donors.add(donor);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return donors;
    }

    public boolean removeDonor(int donorId) {
        String sql = "DELETE FROM donors WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, donorId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
