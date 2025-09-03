package org.example.donation_application.controllers;
import org.example.donation_application.Main;
import org.example.donation_application.dao.DonorDAO;
import org.example.donation_application.dao.UserDAO;
import org.example.donation_application.models.Donor;
import org.example.donation_application.utils.ValidationUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class DonorRegistrationController {
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField phoneField;
    @FXML private TextField organizationField;
    @FXML private Button uploadPictureButton;
    @FXML private ImageView pictureImageView;
    @FXML private Label messageLabel;

    private DonorDAO donorDAO = new DonorDAO();
    private UserDAO userDAO = new UserDAO();
    private byte[] selectedPictureBytes;

    @FXML
    private void initialize() {
        System.out.println("DonorRegistrationController initialized");
    }

    @FXML
    private void handleUploadPicture() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Donor Picture");

        // Set extension filters
        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter(
                "Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"
        );
        fileChooser.getExtensionFilters().add(imageFilter);

        File selectedFile = fileChooser.showOpenDialog(uploadPictureButton.getScene().getWindow());

        if (selectedFile != null) {
            try {
                // Validate file size (limit to 5MB)
                if (selectedFile.length() > 5 * 1024 * 1024) {
                    showMessage("Image file size should be less than 5MB", true);
                    return;
                }

                // Convert file to byte array
                selectedPictureBytes = convertFileToByteArray(selectedFile);

                // Display image in ImageView
                Image image = new Image(selectedFile.toURI().toString());
                pictureImageView.setImage(image);
                pictureImageView.setVisible(true);

                uploadPictureButton.setText("Change Picture");
                showMessage("Picture uploaded successfully", false);

            } catch (IOException e) {
                showMessage("Error uploading picture: " + e.getMessage(), true);
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleRegister() {
        if (!validateInput()) {
            return;
        }

        String email = emailField.getText().trim();
        if (userDAO.isEmailExists(email)) {
            showMessage("Email already exists in the system", true);
            return;
        }

        Donor donor = new Donor(
                nameField.getText().trim(),
                email,
                passwordField.getText(),
                phoneField.getText().trim(),
                organizationField.getText().trim()
        );

        // Set picture if uploaded
        if (selectedPictureBytes != null) {
            donor.setDonorPicture(selectedPictureBytes);
        }

        if (donorDAO.registerDonor(donor)) {
            showMessage("Registration successful! You can now login.", false);
            clearFields();
        } else {
            showMessage("Registration failed. Please try again.", true);
        }
    }

    @FXML
    private void handleBackToLogin() {
        Main.showLoginScreen();
    }

    private boolean validateInput() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String phone = phoneField.getText().trim();

        if (ValidationUtils.isEmpty(name) || ValidationUtils.isEmpty(email) ||
                ValidationUtils.isEmpty(password) || ValidationUtils.isEmpty(phone)) {
            showMessage("Please fill in all required fields", true);
            return false;
        }

        if (!ValidationUtils.isValidName(name)) {
            showMessage("Please enter a valid name", true);
            return false;
        }

        if (!ValidationUtils.isValidEmail(email)) {
            showMessage("Please enter a valid email address", true);
            return false;
        }

        if (!ValidationUtils.isValidPassword(password)) {
            showMessage("Password must be at least 6 characters long", true);
            return false;
        }

        if (!password.equals(confirmPassword)) {
            showMessage("Passwords do not match", true);
            return false;
        }

        if (!ValidationUtils.isValidPhone(phone)) {
            showMessage("Please enter a valid phone number (10-15 digits)", true);
            return false;
        }

        return true;
    }

    private void clearFields() {
        nameField.clear();
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        phoneField.clear();
        organizationField.clear();
        pictureImageView.setImage(null);
        pictureImageView.setVisible(false);
        uploadPictureButton.setText("Upload Picture");
        selectedPictureBytes = null;
    }

    private void showMessage(String message, boolean isError) {
        messageLabel.setText(message);
        messageLabel.getStyleClass().removeAll("message-error", "message-success");
        messageLabel.getStyleClass().add(isError ? "message-error" : "message-success");
    }

    private byte[] convertFileToByteArray(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }

            return baos.toByteArray();
        }
    }
}
