package org.example.donation_application.controllers;

import org.example.donation_application.Main;
import org.example.donation_application.models.Student;
import org.example.donation_application.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class PendingApprovalController {

    @FXML private Label nameLabel;
    @FXML private Label emailLabel;
    @FXML private Label messageLabel;

    @FXML
    private void initialize() {
        SessionManager sessionManager = SessionManager.getInstance();
        Student student = (Student) sessionManager.getCurrentUser();

        nameLabel.setText("Name: " + student.getName());
        emailLabel.setText("Email: " + student.getEmail());
        messageLabel.setText("Your account is pending approval by the moderator. Please wait for confirmation.");
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        Main.showLoginScreen();
    }
}
