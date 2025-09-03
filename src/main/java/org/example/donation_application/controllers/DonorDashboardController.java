package org.example.donation_application.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import org.example.donation_application.Main;
import org.example.donation_application.dao.DonationDAO;
import org.example.donation_application.dao.StudentDAO;
import org.example.donation_application.models.Donation;
import org.example.donation_application.models.Donor;
import org.example.donation_application.models.Student;
import org.example.donation_application.utils.SessionManager;
import org.example.donation_application.service.DonationReportService;

import java.io.IOException;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.List;
import java.util.Optional;

public class DonorDashboardController {

    // Navigation and UI Components
    @FXML private Label welcomeLabel;
    @FXML private ComboBox<Student.Stream> streamFilterComboBox;
    @FXML private FlowPane studentsFlowPane;
    @FXML private ScrollPane studentsScrollPane;
    @FXML private HBox selectedStudentInfo;
    @FXML private Label selectedStudentLabel;
    @FXML private Button clearSelectionButton;
    @FXML private Button donateButton;
    @FXML private VBox noStudentsMessage;
    @FXML private Button generateReportButton;
    @FXML private Button exportDonationListButton;
    @FXML private Button quickReportButton; // If you add the optional one

    // Donations Table (unchanged)
    @FXML private TableView<Donation> donationsTable;
    @FXML private TableColumn<Donation, String> recipientColumn;
    @FXML private TableColumn<Donation, String> amountColumn;
    @FXML private TableColumn<Donation, String> descriptionColumn;
    @FXML private TableColumn<Donation, String> dateColumn;

    // Data Access Objects
    private StudentDAO studentDAO = new StudentDAO();
    private DonationDAO donationDAO = new DonationDAO();

    // Selected Student Management
    private Student selectedStudent = null;

    @FXML
    private void initialize() {
        // Set TableView resize policy
        donationsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        setupDonationsTable();
        setupStreamFilter();
        loadDonorInfo();
        loadStudents(null); // Load all students initially
        loadDonations();
        updateSelectedStudentUI();
    }


    /**
     * Sets up the donations history table
     */
    private void setupDonationsTable() {
        recipientColumn.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("donationDate"));
    }

    /**
     * Sets up the stream filter ComboBox
     */
    private void setupStreamFilter() {
        // Add "All Streams" option (null value)
        streamFilterComboBox.getItems().add(null);
        streamFilterComboBox.getItems().addAll(Student.Stream.values());
        streamFilterComboBox.setPromptText("All Streams");

        // Set up custom cell factory to display "All Streams" for null value
        streamFilterComboBox.setButtonCell(new ListCell<Student.Stream>() {
            @Override
            protected void updateItem(Student.Stream item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("All Streams");
                } else {
                    setText(item.getValue());
                }
            }
        });

        streamFilterComboBox.setCellFactory(listView -> new ListCell<Student.Stream>() {
            @Override
            protected void updateItem(Student.Stream item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText("");
                } else if (item == null) {
                    setText("All Streams");
                } else {
                    setText(item.getValue());
                }
            }
        });
    }

    /**
     * Loads donor information and updates welcome label
     */
    private void loadDonorInfo() {
        SessionManager sessionManager = SessionManager.getInstance();
        Donor donor = (Donor) sessionManager.getCurrentUser();
        welcomeLabel.setText("Welcome, " + donor.getName() + "!");
    }
    private void loadStudents(Student.Stream filterStream) {
        studentsFlowPane.getChildren().clear();

        List<Student> students;
        if (filterStream == null) {
            students = studentDAO.getAllStudents();
        } else {
            students = studentDAO.getStudentsByStream(filterStream);
        }

        if (students.isEmpty()) {
            noStudentsMessage.setVisible(true);
            noStudentsMessage.setManaged(true);
        } else {
            noStudentsMessage.setVisible(false);
            noStudentsMessage.setManaged(false);

            for (Student student : students) {
                try {
                    // CORRECT PATH based on your structure:
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/donation_application/student-card.fxml"));
                    Node studentCard = loader.load();
                    StudentCardController cardController = loader.getController();

                    cardController.setStudent(student);
                    cardController.setOnCardClickCallback(this::handleStudentCardClick);
                    cardController.setOnDetailViewCallback(this::showStudentDetails);
                    studentsFlowPane.getChildren().add(studentCard);
                } catch (IOException e) {
                    System.err.println("Error loading student card: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * Handles student card click events
     * @param student The clicked student
     */
    private void handleStudentCardClick(Student student) {
        this.selectedStudent = student;
        updateSelectedStudentUI();
        System.out.println("Selected student: " + student.getName());
    }

    /**
     * Updates the UI to reflect the currently selected student
     */
    private void updateSelectedStudentUI() {
        if (selectedStudent == null) {
            selectedStudentInfo.setVisible(false);
            selectedStudentInfo.setManaged(false);
            donateButton.setDisable(true);
        } else {
            selectedStudentLabel.setText(selectedStudent.getName() + " (" +
                    selectedStudent.getSchool() + ", " + selectedStudent.getStream().getValue() + ")");
            selectedStudentInfo.setVisible(true);
            selectedStudentInfo.setManaged(true);
            donateButton.setDisable(false);
        }
    }

    /**
     * Handles stream filter selection
     */
    @FXML
    private void handleStreamFilter() {
        Student.Stream selectedStream = streamFilterComboBox.getValue();
        loadStudents(selectedStream);
        clearSelection(); // Clear selection when filter changes
    }

    /**
     * Clears the current student selection
     */
    @FXML
    private void handleClearSelection() {
        clearSelection();
    }

    /**
     * Internal method to clear selection
     */
    private void clearSelection() {
        this.selectedStudent = null;
        updateSelectedStudentUI();
    }

    /**
     * Loads donation history for the current donor
     */
    private void loadDonations() {
        SessionManager sessionManager = SessionManager.getInstance();
        Donor donor = (Donor) sessionManager.getCurrentUser();

        donationsTable.getItems().clear();
        donationsTable.getItems().addAll(donationDAO.getDonationsByDonor(donor.getId()));
    }

    /**
     * Handles donation process
     */
    @FXML
    private void handleDonate() {
        if (selectedStudent == null) {
            showAlert("Please select a student to donate to.");
            return;
        }

        Dialog<Donation> dialog = createDonationDialog(selectedStudent);
        Optional<Donation> result = dialog.showAndWait();

        result.ifPresent(donation -> {
            SessionManager sessionManager = SessionManager.getInstance();
            Donor donor = (Donor) sessionManager.getCurrentUser();

            donation.setDonorId(donor.getId());
            donation.setStudentId(selectedStudent.getId());

            if (donationDAO.createDonation(donation)) {
                showAlert("Donation successful!");
                loadDonations(); // Refresh donation history
                clearSelection(); // Clear selection after successful donation
            } else {
                showAlert("Donation failed. Please try again.");
            }
        });
    }

    /**
     * Creates the donation dialog
     * @param student The student to donate to
     * @return The configured dialog
     */
//    private Dialog<Donation> createDonationDialog(Student student) {
//        Dialog<Donation> dialog = new Dialog<>();
//        dialog.setTitle("Make Donation");
//        dialog.setHeaderText("Donate to: " + student.getName());
//
//        ButtonType donateButtonType = new ButtonType("Donate", ButtonBar.ButtonData.OK_DONE);
//        dialog.getDialogPane().getButtonTypes().addAll(donateButtonType, ButtonType.CANCEL);
//
//        TextField amountField = new TextField();
//        amountField.setPromptText("Amount");
//
//        TextArea descriptionArea = new TextArea();
//        descriptionArea.setPromptText("Description (optional)");
//        descriptionArea.setPrefRowCount(3);
//
//        VBox content = new VBox();
//        content.setSpacing(10.0);
//        content.getChildren().addAll(
//                new Label("Amount:"), amountField,
//                new Label("Description:"), descriptionArea
//        );
//
//        dialog.getDialogPane().setContent(content);
//
//        dialog.setResultConverter(dialogButton -> {
//            if (dialogButton == donateButtonType) {
//                try {
//                    BigDecimal amount = new BigDecimal(amountField.getText());
//                    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
//                        showAlert("Please enter a valid amount greater than 0.");
//                        return null;
//                    }
//                    return new Donation(0, 0, amount, descriptionArea.getText());
//                } catch (NumberFormatException e) {
//                    showAlert("Please enter a valid numeric amount.");
//                    return null;
//                }
//            }
//            return null;
//        });
//
//        return dialog;
//    }
    private Dialog<Donation> createDonationDialog(Student student) {
        Dialog<Donation> dialog = new Dialog<>();
        dialog.setTitle("Make Donation");
        dialog.setHeaderText("Donate to: " + student.getName());

        ButtonType donateButtonType = new ButtonType("Donate", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(donateButtonType, ButtonType.CANCEL);

        // Input fields
        TextField amountField = new TextField();
        amountField.setPromptText("Amount");
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Description (optional)");
        descriptionArea.setPrefRowCount(3);

        // Fee breakdown labels (initially empty)
        Label originalAmountLabel = new Label("Your donation: $0.00");
        Label platformFeeLabel = new Label("Platform fee (2%): $0.00");
        Label netAmountLabel = new Label("Amount to student: $0.00");

        // Style the labels
        originalAmountLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        platformFeeLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");
        netAmountLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 12px;");

        // Add separator for visual clarity
        Separator separator = new Separator();

        // Create content layout
        VBox content = new VBox();
        content.setSpacing(10.0);
        content.getChildren().addAll(
                new Label("Amount:"), amountField,
                separator,
                new Label("Fee Breakdown:"),
                originalAmountLabel,
                platformFeeLabel,
                netAmountLabel,
                new Label("Description:"), descriptionArea
        );

        // Add listener to amount field for real-time calculation
        amountField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (newValue == null || newValue.trim().isEmpty()) {
                    // Reset labels when field is empty
                    originalAmountLabel.setText("Your donation: $0.00");
                    platformFeeLabel.setText("Platform fee (2%): $0.00");
                    netAmountLabel.setText("Amount to student: $0.00");
                } else {
                    // Parse the amount and calculate fees
                    BigDecimal originalAmount = new BigDecimal(newValue.trim());

                    if (originalAmount.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal feeRate = new BigDecimal("0.02");
                        BigDecimal platformFee = originalAmount.multiply(feeRate).setScale(2, RoundingMode.HALF_UP);
                        BigDecimal netAmount = originalAmount.subtract(platformFee);

                        // Update labels with calculated values
                        originalAmountLabel.setText(String.format("Your donation: $%.2f", originalAmount));
                        platformFeeLabel.setText(String.format("Platform fee (2%%): -$%.2f", platformFee));
                        netAmountLabel.setText(String.format("Amount to student: $%.2f", netAmount));
                    } else {
                        // Handle negative or zero amounts
                        originalAmountLabel.setText("Your donation: $" + newValue);
                        platformFeeLabel.setText("Platform fee (2%): $0.00");
                        netAmountLabel.setText("Amount to student: $0.00");
                    }
                }
            } catch (NumberFormatException e) {
                // Handle invalid input - keep showing previous valid values or zeros
                originalAmountLabel.setText("Your donation: Invalid amount");
                platformFeeLabel.setText("Platform fee (2%): $0.00");
                netAmountLabel.setText("Amount to student: $0.00");
            }
        });

        dialog.getDialogPane().setContent(content);

        // Update result converter to use net amount
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == donateButtonType) {
                try {
                    String amountText = amountField.getText().trim();
                    if (amountText.isEmpty()) {
                        showAlert("Please enter a donation amount.");
                        return null;
                    }

                    BigDecimal originalAmount = new BigDecimal(amountText);
                    if (originalAmount.compareTo(BigDecimal.ZERO) <= 0) {
                        showAlert("Please enter a valid amount greater than 0.");
                        return null;
                    }

                    // Calculate platform fee and net amount
                    BigDecimal feeRate = new BigDecimal("0.02");
                    BigDecimal platformFee = originalAmount.multiply(feeRate).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal netAmount = originalAmount.subtract(platformFee);

                    // Create donation with net amount (what actually goes to student)
                    Donation donation = new Donation(0, 0, netAmount, descriptionArea.getText());

                    // Optional: Store original amount and fee for potential future use
                    // You'd need to add these fields to your Donation model as transient fields
                    // donation.setOriginalAmount(originalAmount);
                    // donation.setPlatformFee(platformFee);

                    return donation;

                } catch (NumberFormatException e) {
                    showAlert("Please enter a valid numeric amount.");
                    return null;
                }
            }
            return null;
        });

        return dialog;
    }


    /**
     * Refreshes all data and resets filters
     */
    @FXML
    private void handleRefresh() {
        streamFilterComboBox.setValue(null); // Reset filter to "All Streams"
        loadStudents(null); // Load all students
        loadDonations();
        clearSelection();
    }

    /**
     * Handles user logout
     */
    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        Main.showLoginScreen();
    }

    /**
     * Shows an alert message to the user
     * @param message The message to display
     */
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void showStudentDetails(Student student) {
        try {
            // METHOD 1: Set location explicitly
            FXMLLoader loader = new FXMLLoader();
            URL fxmlLocation = getClass().getResource("/org/example/donation_application/student-detail.fxml");

            if (fxmlLocation == null) {
                System.err.println("student-details.fxml not found!");
                return;
            }

            loader.setLocation(fxmlLocation);
            Parent root = loader.load();

            // Get controller and set student data
            StudentDetailsController controller = loader.getController();
            controller.setStudent(student);

            // Create and show stage
            Stage stage = new Stage();
            stage.setTitle("Student Details - " + student.getName());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();

        } catch (IOException e) {
            System.err.println("Error opening student details: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private DonationReportService reportService = new DonationReportService();

    @FXML
    private void handleGenerateMyReport() {
        try {
            SessionManager sessionManager = SessionManager.getInstance();
            Donor currentDonor = (Donor) sessionManager.getCurrentUser();

            // Show file save dialog
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save My Donation Report");
            fileChooser.setInitialFileName(currentDonor.getName().replaceAll("[^a-zA-Z0-9]", "_") + "_donation_report.pdf");
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf");
            fileChooser.getExtensionFilters().add(extFilter);

            File file = fileChooser.showSaveDialog(welcomeLabel.getScene().getWindow());

            if (file != null) {
                boolean success = reportService.generateDonorReport(currentDonor, file.getAbsolutePath());

                if (success) {
                    showAlert("Report generated successfully!\nSaved to: " + file.getAbsolutePath());
                } else {
                    showAlert("Failed to generate report. Please try again.");
                }
            }

        } catch (Exception e) {
            System.err.println("Error generating report: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleGenerateAllDonations() {
        try {
            SessionManager sessionManager = SessionManager.getInstance();
            Donor currentDonor = (Donor) sessionManager.getCurrentUser();

            List<Donation> allDonations = donationDAO.getDonationsByDonor(currentDonor.getId());

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save All Donations List");
            fileChooser.setInitialFileName("all_donations_" + System.currentTimeMillis() + ".pdf");
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf");
            fileChooser.getExtensionFilters().add(extFilter);

            File file = fileChooser.showSaveDialog(welcomeLabel.getScene().getWindow());

            if (file != null) {
                boolean success = reportService.generateDonationList(allDonations, file.getAbsolutePath(), "My Donation History");

                if (success) {
                    showAlert("Donation list generated successfully!\nSaved to: " + file.getAbsolutePath());
                } else {
                    showAlert("Failed to generate donation list. Please try again.");
                }
            }

        } catch (Exception e) {
            System.err.println("Error generating donation list: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error: " + e.getMessage());
        }
    }
    @FXML
    private void handleExportDonationList() {
        try {
            // Get current donor from session
            SessionManager sessionManager = SessionManager.getInstance();
            Donor currentDonor = (Donor) sessionManager.getCurrentUser();

            if (currentDonor == null) {
                showAlert("Error: No donor session found. Please log in again.");
                return;
            }

            // Fetch donations for current donor
            List<Donation> donations = donationDAO.getDonationsByDonor(currentDonor.getId());

            if (donations.isEmpty()) {
                showAlert("No donations found to export.");
                return;
            }

            // Show file save dialog
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Donation List");
            fileChooser.setInitialFileName(currentDonor.getName().replaceAll("[^a-zA-Z0-9]", "_") + "_donation_list.pdf");

            // Add PDF file filter
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf");
            fileChooser.getExtensionFilters().add(extFilter);

            // Show save dialog
            File file = fileChooser.showSaveDialog(welcomeLabel.getScene().getWindow());

            if (file != null) {
                // Create report service and generate PDF
                DonationReportService reportService = new DonationReportService();
                boolean success = reportService.generateDonationList(
                        donations,
                        file.getAbsolutePath(),
                        "Donation History - " + currentDonor.getName()
                );

                if (success) {
                    showAlert("Donation list exported successfully!\nSaved to: " + file.getAbsolutePath());
                } else {
                    showAlert("Failed to export donation list. Please try again.");
                }
            }

        } catch (Exception e) {
            System.err.println("Error exporting donation list: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error occurred while exporting: " + e.getMessage());
        }
    }
}
