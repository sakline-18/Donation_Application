package org.example.donation_application.controllers;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
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
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.collections.FXCollections;

import org.example.donation_application.Main;
import org.example.donation_application.dao.DonationDAO;
import org.example.donation_application.dao.DonorDAO;
import org.example.donation_application.dao.StudentDAO;
import org.example.donation_application.models.Donation;
import org.example.donation_application.models.Donor;
import org.example.donation_application.models.Moderator;
import org.example.donation_application.models.Student;
import org.example.donation_application.service.DonationReportService;
import org.example.donation_application.utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ModeratorDashboardController {

    // Navigation
    @FXML
    private Label welcomeLabel;

    // Pending Students Tab
    @FXML
    private FlowPane pendingFlowPane;
    @FXML
    private ScrollPane pendingScrollPane;
    @FXML
    private HBox selectedPendingInfo;
    @FXML
    private Label selectedPendingLabel;
    @FXML
    private Button approveButton;
    @FXML
    private Button rejectButton;
    @FXML
    private Button clearPendingSelectionButton;
    @FXML
    private VBox noPendingMessage;

    // Approved Students Tab
    @FXML
    private FlowPane studentsFlowPane;
    @FXML
    private ScrollPane studentsScrollPane;
    @FXML
    private HBox selectedStudentInfo;
    @FXML
    private Label selectedStudentLabel;
    @FXML
    private Button removeStudentButton;
    @FXML
    private Button clearStudentSelectionButton;
    @FXML
    private VBox noStudentsMessage;

    // Donors Table (unchanged)
    @FXML
    private TableView<Donor> donorsTable;
    @FXML
    private TableColumn<Donor, String> donorNameColumn;
    @FXML
    private TableColumn<Donor, String> donorEmailColumn;
    @FXML
    private TableColumn<Donor, String> donorOrgColumn;
    @FXML
    private TableColumn<Donor, String> donorPhoneColumn;
    // Donor Cards Components (add these to your existing @FXML fields)
    @FXML
    private FlowPane donorsFlowPane;
    @FXML
    private ScrollPane donorsScrollPane;
    @FXML
    private HBox selectedDonorInfo;
    @FXML
    private Label selectedDonorLabel;
    @FXML
    private Button clearDonorSelectionButton;
    @FXML
    private VBox noDonorsMessage;

    // Transactions Tab Components
    @FXML
    private TableView<Donation> transactionsTable;
    @FXML
    private TableColumn<Donation, String> transactionDonorColumn;
    @FXML
    private TableColumn<Donation, String> transactionStudentColumn;
    @FXML
    private TableColumn<Donation, BigDecimal> transactionAmountColumn;
    @FXML
    private TableColumn<Donation, String> transactionDescriptionColumn;
    @FXML
    private TableColumn<Donation, LocalDateTime> transactionDateColumn;
    @FXML
    private DatePicker dateFromPicker;
    @FXML
    private DatePicker dateToPicker;
    @FXML
    private TextField minAmountField;
    @FXML
    private TextField maxAmountField;
    @FXML
    private Button filterTransactionsButton;
    @FXML
    private Button clearFiltersButton;
    @FXML
    private Button generateAllDonationsReportButton;

    // Collections for managing card controllers
    private List<DonorCardController> donorCardControllers = new ArrayList<>();
    private Donor selectedDonor = null;
    // Data Access Objects
    private StudentDAO studentDAO = new StudentDAO();
    private DonorDAO donorDAO = new DonorDAO();
    private DonationDAO donationDAO = new DonationDAO(); // Add this line

    // Collections for managing card controllers

    // Add this line for transactions data
    private ObservableList<Donation> allTransactions = FXCollections.observableArrayList();


    // Selected Students
    private Student selectedPendingStudent = null;
    private Student selectedApprovedStudent = null;

    @FXML
    private void initialize() {
//        setupDonorsTable();
        loadModeratorInfo();
        loadAllData();
        updateSelectedStudentUIs();
        setupTransactionsTable();
        handleRefreshTransactions();
    }

    private void setupDonorsTable() {
        donorNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        donorEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        donorOrgColumn.setCellValueFactory(new PropertyValueFactory<>("organization"));
        donorPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        donorsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

    }

    private void loadModeratorInfo() {
        SessionManager sessionManager = SessionManager.getInstance();
        Moderator moderator = (Moderator) sessionManager.getCurrentUser();
        welcomeLabel.setText("Welcome, " + moderator.getName() + " (Moderator)");
    }

    private void loadAllData() {
        loadPendingStudents();
        loadApprovedStudents();
        loadDonors();
    }

    private void loadPendingStudents() {
        pendingFlowPane.getChildren().clear();

        List<Student> pendingStudents = studentDAO.getPendingStudents();

        if (pendingStudents.isEmpty()) {
            noPendingMessage.setVisible(true);
            noPendingMessage.setManaged(true);
        } else {
            noPendingMessage.setVisible(false);
            noPendingMessage.setManaged(false);

            for (Student student : pendingStudents) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/donation_application/student-card.fxml"));
                    Node studentCard = loader.load();
                    StudentCardController cardController = loader.getController();

                    cardController.setStudent(student);
                    cardController.setOnCardClickCallback(this::handlePendingStudentCardClick);
                    cardController.setOnDetailViewCallback(this::showStudentDetails); // Add this line

                    pendingFlowPane.getChildren().add(studentCard);
                } catch (IOException e) {
                    System.err.println("Error loading pending student card: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private void loadApprovedStudents() {
        studentsFlowPane.getChildren().clear();

        List<Student> approvedStudents = studentDAO.getAllStudents();

        if (approvedStudents.isEmpty()) {
            noStudentsMessage.setVisible(true);
            noStudentsMessage.setManaged(true);
        } else {
            noStudentsMessage.setVisible(false);
            noStudentsMessage.setManaged(false);

            for (Student student : approvedStudents) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/donation_application/student-card.fxml"));
                    Node studentCard = loader.load();
                    StudentCardController cardController = loader.getController();

                    cardController.setStudent(student);
                    cardController.setOnCardClickCallback(this::handleApprovedStudentCardClick);
                    cardController.setOnDetailViewCallback(this::showStudentDetails); // Add this line

                    studentsFlowPane.getChildren().add(studentCard);
                } catch (IOException e) {
                    System.err.println("Error loading approved student card: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private void handlePendingStudentCardClick(Student student) {
        this.selectedPendingStudent = student;
        updateSelectedStudentUIs();
        System.out.println("Selected pending student: " + student.getName());
    }

    private void handleApprovedStudentCardClick(Student student) {
        this.selectedApprovedStudent = student;
        updateSelectedStudentUIs();
        System.out.println("Selected approved student: " + student.getName());
    }

    private void updateSelectedStudentUIs() {
        // Update pending student selection UI
        if (selectedPendingStudent == null) {
            selectedPendingInfo.setVisible(false);
            selectedPendingInfo.setManaged(false);
        } else {
            selectedPendingLabel.setText(selectedPendingStudent.getName() + " (" +
                    selectedPendingStudent.getSchool() + ", " + selectedPendingStudent.getStream().getValue() + ")");
            selectedPendingInfo.setVisible(true);
            selectedPendingInfo.setManaged(true);
        }

        // Update approved student selection UI
        if (selectedApprovedStudent == null) {
            selectedStudentInfo.setVisible(false);
            selectedStudentInfo.setManaged(false);
        } else {
            selectedStudentLabel.setText(selectedApprovedStudent.getName() + " (" +
                    selectedApprovedStudent.getSchool() + ", " + selectedApprovedStudent.getStream().getValue() + ")");
            selectedStudentInfo.setVisible(true);
            selectedStudentInfo.setManaged(true);
        }
    }

    private void loadDonors() {
        donorCardControllers.clear();
        donorsFlowPane.getChildren().clear();

        List<Donor> donors = donorDAO.getAllDonors();

        if (donors.isEmpty()) {
            noDonorsMessage.setVisible(true);
            noDonorsMessage.setManaged(true);
        } else {
            noDonorsMessage.setVisible(false);
            noDonorsMessage.setManaged(false);

            for (Donor donor : donors) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/donation_application/donor-card.fxml"));
                    Node donorCard = loader.load();
                    DonorCardController cardController = loader.getController();

                    cardController.setDonor(donor);
                    cardController.setOnCardClickCallback(this::handleDonorCardClick);
                    cardController.setOnDetailViewCallback(this::showDonorDetails);

                    donorCardControllers.add(cardController);
                    donorsFlowPane.getChildren().add(donorCard);
                } catch (IOException e) {
                    System.err.println("Error loading donor card: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    @FXML
    private void handleApproveStudent() {
        if (selectedPendingStudent == null) {
            showAlert("Please select a pending student to approve.");
            return;
        }

        if (confirmAction("Approve Student", "Are you sure you want to approve " + selectedPendingStudent.getName() + "?")) {
            if (studentDAO.approveStudent(selectedPendingStudent.getId())) {
                showAlert("Student approved successfully!");
                clearPendingSelection();
                loadAllData(); // Refresh both pending and approved lists
            } else {
                showAlert("Failed to approve student. Please try again.");
            }
        }
    }

    @FXML
    private void handleRejectStudent() {
        if (selectedPendingStudent == null) {
            showAlert("Please select a pending student to reject.");
            return;
        }

        if (confirmAction("Reject Student", "Are you sure you want to reject " + selectedPendingStudent.getName() + "? This action cannot be undone.")) {
            if (studentDAO.rejectStudent(selectedPendingStudent.getId())) {
                showAlert("Student rejected and removed from system.");
                clearPendingSelection();
                loadAllData();
            } else {
                showAlert("Failed to reject student. Please try again.");
            }
        }
    }

    @FXML
    private void handleRemoveStudent() {
        if (selectedApprovedStudent == null) {
            showAlert("Please select a student to remove.");
            return;
        }

        if (confirmAction("Remove Student", "Are you sure you want to remove " + selectedApprovedStudent.getName() + "? This will delete all their data.")) {
            if (studentDAO.removeStudent(selectedApprovedStudent.getId())) {
                showAlert("Student removed successfully!");
                clearStudentSelection();
                loadAllData();
            } else {
                showAlert("Failed to remove student. Please try again.");
            }
        }
    }

    @FXML
    private void handleRemoveDonor() {
        if (selectedDonor == null) {
            showAlert("Please select a donor to remove.");
            return;
        }

        if (confirmAction("Remove Donor", "Are you sure you want to remove " + selectedDonor.getName() + "? This will delete all their data.")) {
            if (donorDAO.removeDonor(selectedDonor.getId())) {
                showAlert("Donor removed successfully!");
                clearDonorSelection();
                loadDonors(); // Refresh the donor cards
            } else {
                showAlert("Failed to remove donor. Please try again.");
            }
        }
    }

    @FXML
    private void handleClearPendingSelection() {
        clearPendingSelection();
    }

    @FXML
    private void handleClearStudentSelection() {
        clearStudentSelection();
    }

    private void clearPendingSelection() {
        selectedPendingStudent = null;
        updateSelectedStudentUIs();
    }

    private void clearStudentSelection() {
        selectedApprovedStudent = null;
        updateSelectedStudentUIs();
    }

    @FXML
    private void handleRefresh() {
        loadPendingStudents();
        loadApprovedStudents();
        loadDonors(); // Add this line
        clearPendingSelection();
        clearStudentSelection();
        clearDonorSelection(); // Add this line
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        Main.showLoginScreen();
    }

    private boolean confirmAction(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

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

    private void handleDonorCardClick(Donor donor) {
        this.selectedDonor = donor;
        updateDonorSelectionUI();
        System.out.println("Selected donor: " + donor.getName());
    }

    private void updateDonorSelectionUI() {
        // Update all cards' visual state
        for (DonorCardController controller : donorCardControllers) {
            boolean shouldBeSelected = selectedDonor != null &&
                    controller.getDonor().getId() == selectedDonor.getId();
            controller.setSelected(shouldBeSelected);
        }

        // Update selection info display
        if (selectedDonor == null) {
            selectedDonorInfo.setVisible(false);
            selectedDonorInfo.setManaged(false);
        } else {
            selectedDonorLabel.setText(selectedDonor.getName() + " (" + selectedDonor.getOrganization() + ")");
            selectedDonorInfo.setVisible(true);
            selectedDonorInfo.setManaged(true);
        }
    }

    private void showDonorDetails(Donor donor) {
        try {
            FXMLLoader loader = new FXMLLoader();
            URL fxmlLocation = getClass().getResource("/org/example/donation_application/donor-details.fxml");

            if (fxmlLocation == null) {
                System.err.println("donor-details.fxml not found!");
                return;
            }

            loader.setLocation(fxmlLocation);
            Parent root = loader.load();

            DonorDetailsController controller = loader.getController();
            controller.setDonor(donor);

            Stage stage = new Stage();
            stage.setTitle("Donor Details - " + donor.getName());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();

        } catch (IOException e) {
            System.err.println("Error opening donor details: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClearDonorSelection() {
        clearDonorSelection();
    }

    private void clearDonorSelection() {
        this.selectedDonor = null;
        updateDonorSelectionUI();
    }


    /**
     * Refresh the transactions table with latest data from database
     */
    @FXML
    private void handleRefreshTransactions() {
        try {
            System.out.println("Refreshing transactions...");

            // Load all transactions from database
            loadAllTransactions();

            // Clear any applied filters
            clearAllFilters();

            System.out.println("Transactions refreshed successfully. Total: " + allTransactions.size());

        } catch (Exception e) {
            System.err.println("Error refreshing transactions: " + e.getMessage());
            e.printStackTrace();
            showAlert("Failed to refresh transactions. Please try again.\nError: " + e.getMessage());
        }
    }

    /**
     * Load all transactions from database and update the table
     */
//    private void loadAllTransactions() {
//        try {
//            // Get all donations with donor and student names
//            List<Donation> allDonations = donationDAO.getAllDonationsWithNames();
//
//            // Update the observable list
//            allTransactions.setAll(allDonations);
//
//            // Set the table items to show all transactions
//            transactionsTable.setItems(allTransactions);
//
//            System.out.println("Loaded " + allDonations.size() + " transactions");
//
//        } catch (Exception e) {
//            System.err.println("Error loading transactions: " + e.getMessage());
//            e.printStackTrace();
//            showAlert("Error loading transactions: " + e.getMessage());
//        }
//    }
    private void loadAllTransactions() {
        try {
            List<Donation> allDonations = donationDAO.getAllDonationsWithNames();

            // DEBUG: Check if data exists
            System.out.println("DEBUG: Loaded " + allDonations.size() + " donations from database");
            for (Donation d : allDonations) {
                System.out.println("DEBUG: Donor=" + d.getDonorName() + ", Student=" + d.getStudentName() + ", Amount=" + d.getAmount());
            }

            allTransactions.setAll(allDonations);
            System.out.println("DEBUG: ObservableList size: " + allTransactions.size());

        } catch (Exception e) {
            System.err.println("Error loading transactions: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Clear all filter inputs and show all transactions
     */
    @FXML
    private void clearAllFilters() {
        // Clear date pickers
        dateFromPicker.setValue(null);
        dateToPicker.setValue(null);

        // Clear amount fields
        minAmountField.clear();
        maxAmountField.clear();

        // Reset table to show all transactions
        transactionsTable.setItems(allTransactions);

        System.out.println("Filters cleared. Showing all " + allTransactions.size() + " transactions");
    }

    //    private void setupTransactionsTable() {
//        // CRITICAL: These strings must EXACTLY match your Donation getter method names
//        transactionDonorColumn.setCellValueFactory(new PropertyValueFactory<>("donorName"));
//        transactionStudentColumn.setCellValueFactory(new PropertyValueFactory<>("studentName"));
//        transactionAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
//        transactionDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
//        transactionDateColumn.setCellValueFactory(new PropertyValueFactory<>("donationDate"));
//
//        // Format amount column
//        transactionAmountColumn.setCellFactory(column -> new TableCell<Donation, BigDecimal>() {
//            @Override
//            protected void updateItem(BigDecimal amount, boolean empty) {
//                super.updateItem(amount, empty);
//                if (empty || amount == null) {
//                    setText(null);
//                } else {
//                    setText(String.format("$%.2f", amount));
//                }
//            }
//        });
//
//        // Format date column
//        transactionDateColumn.setCellFactory(column -> new TableCell<Donation, LocalDateTime>() {
//            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
//
//            @Override
//            protected void updateItem(LocalDateTime date, boolean empty) {
//                super.updateItem(date, empty);
//                if (empty || date == null) {
//                    setText(null);
//                } else {
//                    setText(date.format(formatter));
//                }
//            }
//        });
//
//        // Set the ObservableList
//        transactionsTable.setItems(allTransactions);
//    }
    private void setupTransactionsTable() {
        System.out.println("Setting up transactions table columns...");

        // CRITICAL: These strings must EXACTLY match your Donation getter method names
        transactionDonorColumn.setCellValueFactory(new PropertyValueFactory<>("donorName"));
        transactionStudentColumn.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        transactionAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        transactionDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        transactionDateColumn.setCellValueFactory(new PropertyValueFactory<>("donationDate"));

        // Custom cell factory for amount (currency formatting)
        transactionAmountColumn.setCellFactory(column -> new TableCell<Donation, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", amount));
                }
            }
        });

        // Custom cell factory for date (formatted display)
        transactionDateColumn.setCellFactory(column -> new TableCell<Donation, LocalDateTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");

            @Override
            protected void updateItem(LocalDateTime date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(formatter));
                }
            }
        });

        // Set column widths
        transactionDonorColumn.setPrefWidth(150);
        transactionStudentColumn.setPrefWidth(150);
        transactionAmountColumn.setPrefWidth(100);
        transactionDescriptionColumn.setPrefWidth(200);
        transactionDateColumn.setPrefWidth(150);

        // Set resize policy
        transactionsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Bind the ObservableList to the table
        transactionsTable.setItems(allTransactions);

        System.out.println("Table columns configured successfully");
    }
    /**
     * Generate PDF report for all donations made in the system
     */
    @FXML
    private void handleGenerateAllDonationsReport() {
        try {
            // Get all donations from database
            List<Donation> allDonations = donationDAO.getAllDonationsWithNames();

            if (allDonations.isEmpty()) {
                showAlert("No donations available to generate report.");
                return;
            }

            // Show file save dialog
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save All Donations Report");
            fileChooser.setInitialFileName("all_donations_report_" +
                    java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf");

            // Add PDF file filter
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf");
            fileChooser.getExtensionFilters().add(extFilter);

            // Show save dialog
            File file = fileChooser.showSaveDialog(welcomeLabel.getScene().getWindow());

            if (file != null) {
                // Create report service and generate comprehensive PDF report
                DonationReportService reportService = new DonationReportService();
                boolean success = reportService.generateAllDonationsReport(allDonations, file.getAbsolutePath());

                if (success) {
                    showAlert("All donations report generated successfully!\n" +
                            "Total donations: " + allDonations.size() + "\n" +
                            "Saved to: " + file.getAbsolutePath());
                } else {
                    showAlert("Failed to generate donations report. Please try again.");
                }
            }

        } catch (Exception e) {
            System.err.println("Error generating all donations report: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error occurred while generating report: " + e.getMessage());
        }
    }


}
