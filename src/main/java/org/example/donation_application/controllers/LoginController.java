package org.example.donation_application.controllers;

import org.example.donation_application.Main;
import org.example.donation_application.dao.DatabaseConnection;
import org.example.donation_application.dao.UserDAO;
import org.example.donation_application.models.Student;
import org.example.donation_application.models.User;
import org.example.donation_application.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController {

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
//    @FXML
//    private ComboBox<String> userTypeCombo;
    @FXML
    private Label messageLabel;

    private UserDAO userDAO = new UserDAO();

    @FXML
    private void initialize() {
        System.out.println("\n########################################");
        System.out.println("    LOGIN CONTROLLER INITIALIZED");
        System.out.println("########################################");

//        if (userTypeCombo == null) {
//            System.err.println("❌ ERROR: userTypeCombo is null! Check fx:id in FXML.");
//            return;
//        }
//
//        userTypeCombo.getItems().addAll("Student", "Donor", "Moderator");
//        userTypeCombo.setValue("Moderator"); // Default to moderator for testing

        // Test database connection and moderator on startup
        DatabaseConnection.testModeratorExists();

        System.out.println("✓ LoginController initialized successfully");
        System.out.println("########################################\n");
    }

    @FXML
    private void handleLogin() {
        System.out.println("\n******************************************");
        System.out.println("         LOGIN BUTTON CLICKED");
        System.out.println("******************************************");

        // Get form values
        String email = emailField.getText();
        String password = passwordField.getText();
        //String userType = userTypeCombo.getValue();

        System.out.println("Form values:");
        System.out.println("  Raw email: '" + email + "'");
        System.out.println("  Email length: " + (email != null ? email.length() : "null"));
        System.out.println("  Raw password: '" + password + "'");
        System.out.println("  Password length: " + (password != null ? password.length() : "null"));
//        System.out.println("  Selected user type: '" + userType + "'");

        // Trim email
        String trimmedEmail = email != null ? email.trim() : "";
        System.out.println("  Trimmed email: '" + trimmedEmail + "'");
        System.out.println("  Trimmed email length: " + trimmedEmail.length());

        // Validation
        if (trimmedEmail.isEmpty() || password == null || password.isEmpty()) {
            System.err.println("❌ Validation failed: Empty credentials");
            showMessage("Please fill in all fields", true);
            return;
        }

        // Test with known moderator credentials
        if ("admin@portal.com".equals(trimmedEmail)) {
            System.out.println("🔍 Attempting login with known moderator email");
            if ("admin123".equals(password)) {
                System.out.println("🔍 Using known moderator password");
            } else {
                System.out.println("⚠ Password doesn't match known moderator password");
            }
        }

        System.out.println("\n--- Starting authentication process ---");
        User user = userDAO.authenticateUser(trimmedEmail, password);

        if (user == null) {
            System.err.println("❌ AUTHENTICATION FAILED - No user returned from DAO");
            showMessage("Invalid email or password", true);
            return;
        }

        System.out.println("✓ AUTHENTICATION SUCCESSFUL!");
        System.out.println("  Authenticated user: " + user.getName());
        System.out.println("  User type: " + user.getClass().getSimpleName());

        SessionManager sessionManager = SessionManager.getInstance();

        // Determine user type and redirect accordingly
        if (user instanceof Student) {
            Student student = (Student) user;
            if (student.isPending()) {
                System.out.println("→ Redirecting to pending approval screen");
                sessionManager.setCurrentUser(user, "pending");
                Main.showScene("pending-approval.fxml", "Pending Approval");
            } else {
                System.out.println("→ Redirecting to student dashboard");
                sessionManager.setCurrentUser(user, "student");
                Main.showScene("student-dashboard.fxml", "Student Dashboard");
            }
        } else if (user.getClass().getSimpleName().equals("Donor")) {
            System.out.println("→ Redirecting to donor dashboard");
            sessionManager.setCurrentUser(user, "donor");
            Main.showScene("donor-dashboard.fxml", "Donor Dashboard");
        } else if (user.getClass().getSimpleName().equals("Moderator")) {
            System.out.println("→ Redirecting to moderator dashboard");
            sessionManager.setCurrentUser(user, "moderator");
            Main.showScene("moderator-dashboard.fxml", "Moderator Dashboard");
        } else {
            System.err.println("❌ Unknown user type: " + user.getClass().getSimpleName());
            showMessage("Unknown user type", true);
        }

        System.out.println("******************************************\n");
    }

    @FXML
    private void handleStudentRegister() {
        System.out.println("Student register button clicked");
        Main.showScene("student-registration.fxml", "Student Registration");
    }

    @FXML
    private void handleDonorRegister() {
        System.out.println("Donor register button clicked");
        Main.showScene("donor-registration.fxml", "Donor Registration");
    }

    private void showMessage(String message, boolean isError) {
        System.out.println("Showing message: " + message + " (isError: " + isError + ")");
        if (messageLabel != null) {
            messageLabel.setText(message);
            messageLabel.getStyleClass().removeAll("message-error", "message-success");
            messageLabel.getStyleClass().add(isError ? "message-error" : "message-success");
        }
    }
}
