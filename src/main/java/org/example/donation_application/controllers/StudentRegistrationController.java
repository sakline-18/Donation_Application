package org.example.donation_application.controllers;
import org.example.donation_application.Main;
import org.example.donation_application.dao.StudentDAO;
import org.example.donation_application.dao.UserDAO;
import org.example.donation_application.models.Student;
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

public class StudentRegistrationController {
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField phoneField;
    @FXML private TextArea addressArea;
    @FXML private TextField schoolField;
    @FXML private ComboBox<Student.Stream> streamComboBox;
    @FXML private TextArea reasonTextArea;
    @FXML private Button uploadPictureButton;
    @FXML private ImageView pictureImageView;
    @FXML private Label messageLabel;
    @FXML private Button registerButton;
    @FXML private Button backButton;
    @FXML private TextField needAmountField;

    private StudentDAO studentDAO = new StudentDAO();
    private UserDAO userDAO = new UserDAO();
    private byte[] selectedPictureBytes;

    @FXML
    private void initialize() {
        System.out.println("StudentRegistrationController initialized");

        // Initialize stream ComboBox
        streamComboBox.getItems().addAll(Student.Stream.values());
        streamComboBox.setPromptText("Select Stream");

        needAmountField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue.matches("\\d*")){
                needAmountField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        // Set placeholder text for reason field
        reasonTextArea.setPromptText("Please explain why you need financial assistance...");
        needAmountField.setPromptText("Enter amount needed.");
    }

    @FXML
    private void handleUploadPicture() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Student Picture");

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
        System.out.println("Register button clicked");
        if (!validateInput()) {
            return;
        }

        String email = emailField.getText().trim();
        if (userDAO.isEmailExists(email)) {
            showMessage("Email already exists in the system", true);
            return;
        }

        Student student = new Student(
                nameField.getText().trim(),
                email,
                passwordField.getText(),
                phoneField.getText().trim(),
                addressArea.getText().trim(),
                schoolField.getText().trim(),
                streamComboBox.getValue(),
                reasonTextArea.getText().trim()
        );
        try {
            String needAmountText = needAmountField.getText().trim();
            if (!needAmountText.isEmpty()) {
                int needAmount = Integer.parseInt(needAmountText);
                if (needAmount < 0) {
                    showMessage("Need Amount cannot be negative", true);
                    return; // ADD RETURN TO PREVENT REGISTRATION
                }
                student.setNeedAmount(needAmount); // ADD THIS LINE
            } else {
                student.setNeedAmount(0); // Set default for empty field
            }
        } catch (NumberFormatException e) {
            showMessage("Please enter a valid number for Need Amount", true);
            return; // ADD RETURN TO PREVENT REGISTRATION
        }

        // Set picture if uploaded
        if (selectedPictureBytes != null) {
            student.setStudentPicture(selectedPictureBytes);
        }

        try {
            if (studentDAO.registerStudent(student)) {
                showMessage("Registration successful! Please wait for moderator approval.", false);
                clearFields();
            } else {
                showMessage("Registration failed. Please try again.", true);
            }
        } catch (Exception e) {
            System.err.println("Error during registration:");
            e.printStackTrace();
            showMessage("Registration failed due to database error.", true);
        }
    }

    @FXML
    private void handleBackToLogin() {
        System.out.println("Back to login clicked");
        Main.showLoginScreen();
    }

//    private boolean validateInput() {
//        String name = nameField.getText().trim();
//        String email = emailField.getText().trim();
//        String password = passwordField.getText();
//        String confirmPassword = confirmPasswordField.getText();
//        String phone = phoneField.getText().trim();
//        String address = addressArea.getText().trim();
//        String school = schoolField.getText().trim();
//        Student.Stream stream = streamComboBox.getValue();
//        String reason = reasonTextArea.getText().trim();
//        String needAmountText = needAmountField.getText().trim();
//
//        if (ValidationUtils.isEmpty(name) || ValidationUtils.isEmpty(email) ||
//                ValidationUtils.isEmpty(password) || ValidationUtils.isEmpty(phone) ||
//                ValidationUtils.isEmpty(address) || ValidationUtils.isEmpty(school) ||
//                stream == null || ValidationUtils.isEmpty(reason)) {
//            showMessage("Please fill in all fields", true);
//            return false;
//        }
//
//        if (!ValidationUtils.isValidName(name)) {
//            showMessage("Please enter a valid name", true);
//            return false;
//        }
//
//        if (!ValidationUtils.isValidEmail(email)) {
//            showMessage("Please enter a valid email address", true);
//            return false;
//        }
//
//        if (!ValidationUtils.isValidPassword(password)) {
//            showMessage("Password must be at least 6 characters long", true);
//            return false;
//        }
//
//        if (!password.equals(confirmPassword)) {
//            showMessage("Passwords do not match", true);
//            return false;
//        }
//
//        if (!ValidationUtils.isValidPhone(phone)) {
//            showMessage("Please enter a valid phone number (10-15 digits)", true);
//            return false;
//        }
//
//        if (reason.length() < 20) {
//            showMessage("Please provide a more detailed reason (at least 20 characters)", true);
//            return false;
//        }
//
//        if(!needAmountText.isEmpty()){
//            try {
//                int needAmount = Integer.parseInt(needAmountText);
//                if(needAmount < 0){
//                    showMessage("Need Amount cannot be negative", true);
//                    needAmountField.requestFocus();
//                    return false;
//                }
//                if(needAmount > 100000){
//                    showMessage("Need Amount cannot exceed 100000tk", true);
//                    needAmountField.requestFocus();
//                    return false;
//                }
//            }catch (NumberFormatException e){
//                showMessage("Please enter a valid number for Need Amount", true);
//                needAmountField.requestFocus();
//                return false;
//            }
//        }
//
//        return true;
//    }
private boolean validateInput() {
    String name = nameField.getText().trim();
    String email = emailField.getText().trim();
    String password = passwordField.getText();
    String confirmPassword = confirmPasswordField.getText();
    String phone = phoneField.getText().trim();
    String address = addressArea.getText().trim();
    String school = schoolField.getText().trim();
    Student.Stream stream = streamComboBox.getValue();
    String reason = reasonTextArea.getText().trim();
    String needAmountText = needAmountField.getText().trim();

    if (ValidationUtils.isEmpty(name) || ValidationUtils.isEmpty(email) ||
            ValidationUtils.isEmpty(password) || ValidationUtils.isEmpty(phone) ||
            ValidationUtils.isEmpty(address) || ValidationUtils.isEmpty(school) ||
            stream == null || ValidationUtils.isEmpty(reason)) {
        showMessage("Please fill in all fields", true);
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

    if (reason.length() < 20) {
        showMessage("Please provide a more detailed reason (at least 20 characters)", true);
        return false;
    }

    if(!needAmountText.isEmpty()){
        try {
            int needAmount = Integer.parseInt(needAmountText);
            if(needAmount < 0){
                showMessage("Need Amount cannot be negative", true);
                needAmountField.requestFocus();
                return false;
            }
            if(needAmount > 100000){
                showMessage("Need Amount cannot exceed 100000tk", true);
                needAmountField.requestFocus();
                return false;
            }
        }catch (NumberFormatException e){
            showMessage("Please enter a valid number for Need Amount", true);
            needAmountField.requestFocus();
            return false;
        }
    }

    // **ADD THIS VALIDATION CHECK FOR MANDATORY PICTURE**
    if (selectedPictureBytes == null) {
        showMessage("Please upload a student picture", true);
        uploadPictureButton.requestFocus();
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
        addressArea.clear();
        schoolField.clear();
        streamComboBox.setValue(null);
        reasonTextArea.clear();
        needAmountField.clear();
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
