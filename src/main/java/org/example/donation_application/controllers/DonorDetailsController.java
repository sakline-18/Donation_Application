package org.example.donation_application.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.example.donation_application.models.Donor;
import java.io.ByteArrayInputStream;

public class DonorDetailsController {

    @FXML private ImageView donorImageView;
    @FXML private Label nameLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private Label organizationLabel;
    @FXML private Label addressLabel;
    @FXML private TextArea notesTextArea;
    @FXML private Label totalDonationsLabel;
    @FXML private Label totalAmountLabel;
    @FXML private Label lastDonationLabel;

    private Donor donor;

    @FXML
    private void initialize() {
        try {
            // Initialize default values
            setDefaultImage();

            // Set default text for summary labels
            if (totalDonationsLabel != null) {
                totalDonationsLabel.setText("Total Donations: N/A");
            }
            if (totalAmountLabel != null) {
                totalAmountLabel.setText("Total Amount: N/A");
            }
            if (lastDonationLabel != null) {
                lastDonationLabel.setText("Last Donation: N/A");
            }

        } catch (Exception e) {
            System.err.println("Error in DonorDetailsController initialize: " + e.getMessage());
        }
    }

    /**
     * Sets the donor data and updates the display
     * @param donor The donor to display
     */
    public void setDonor(Donor donor) {
        this.donor = donor;
        updateDonorDisplay();
    }

    /**
     * Updates the display with donor information
     */
    private void updateDonorDisplay() {
        try {
            if (donor == null) return;

            // Basic Information
            if (nameLabel != null) {
                nameLabel.setText(donor.getName());
            }
            if (emailLabel != null) {
                emailLabel.setText("Email: " + donor.getEmail());
            }
            if (phoneLabel != null) {
                phoneLabel.setText("Phone: " + donor.getPhone());
            }
            if (organizationLabel != null) {
                organizationLabel.setText("Organization: " + donor.getOrganization());
            }

            // Notes/Description
            if (notesTextArea != null) {
                String notes = "No additional notes available.";
                // If your Donor model has a notes/description field, use it here
                // notes = donor.getNotes() != null ? donor.getNotes() : notes;
                notesTextArea.setText(notes);
            }

            // Load donor image
            loadDonorImage();

            // Update donation summary (if you have this data available)
            updateDonationSummary();

        } catch (Exception e) {
            System.err.println("Error updating donor display: " + e.getMessage());
        }
    }

    /**
     * Loads and displays the donor's image
     */
    private void loadDonorImage() {
        try {
            if (donorImageView != null) {
                // If your Donor model has an image/photo field, use it here
                // For now, we'll just set a default image
                setDefaultImage();

                if (donor.getDonorPicture() != null && donor.getDonorPicture().length > 0) {
                    try {
                        ByteArrayInputStream bis = new ByteArrayInputStream(donor.getDonorPicture());
                        Image image = new Image(bis);
                        donorImageView.setImage(image);
                        donorImageView.setVisible(true);
                    } catch (Exception e) {
                        System.err.println("Error loading student picture: " + e.getMessage());
                        donorImageView.setVisible(false);
                    }
                } else {
                    setDefaultImage();
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading donor image: " + e.getMessage());
            setDefaultImage();
        }
    }

    /**
     * Sets a default placeholder image
     */
    private void setDefaultImage() {
        try {
            if (donorImageView != null) {
                donorImageView.setImage(null);
                donorImageView.setStyle("-fx-background-color: #e0e0e0; -fx-background-radius: 8;");
            }
        } catch (Exception e) {
            // Ignore styling errors
        }
    }

    /**
     * Updates donation summary statistics
     */
    private void updateDonationSummary() {
        try {
            // This would require additional DAO methods to get donation statistics
            // For now, we'll show placeholder text

            if (totalDonationsLabel != null) {
                totalDonationsLabel.setText("Total Donations: --");
            }
            if (totalAmountLabel != null) {
                totalAmountLabel.setText("Total Amount: --");
            }
            if (lastDonationLabel != null) {
                lastDonationLabel.setText("Last Donation: --");
            }

            /* Uncomment and implement if you want to show actual donation statistics:

            DonationDAO donationDAO = new DonationDAO();

            // Get donation count
            int donationCount = donationDAO.getDonationCountByDonor(donor.getId());
            totalDonationsLabel.setText("Total Donations: " + donationCount);

            // Get total amount
            BigDecimal totalAmount = donationDAO.getTotalAmountByDonor(donor.getId());
            totalAmountLabel.setText("Total Amount: $" + totalAmount.toString());

            // Get last donation date
            LocalDate lastDonation = donationDAO.getLastDonationDateByDonor(donor.getId());
            if (lastDonation != null) {
                lastDonationLabel.setText("Last Donation: " + lastDonation.toString());
            } else {
                lastDonationLabel.setText("Last Donation: Never");
            }
            */

        } catch (Exception e) {
            System.err.println("Error updating donation summary: " + e.getMessage());
        }
    }

    /**
     * Handles the close button click
     */
    @FXML
    private void handleClose() {
        try {
            Stage stage = (Stage) nameLabel.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            System.err.println("Error closing donor details window: " + e.getMessage());
        }
    }

    /**
     * Gets the donor associated with this details view
     * @return The donor object
     */
    public Donor getDonor() {
        return donor;
    }
}
