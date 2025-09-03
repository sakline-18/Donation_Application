package org.example.donation_application.controllers;
import org.example.donation_application.Main;
import org.example.donation_application.dao.DonationDAO;
import org.example.donation_application.models.Donation;
import org.example.donation_application.models.Student;
import org.example.donation_application.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.ByteArrayInputStream;

public class StudentDashboardController {
    @FXML private Label welcomeLabel;
    @FXML private Label nameLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private Label schoolLabel;
    @FXML private Label streamLabel;
    @FXML private Label reasonLabel;
    @FXML private ImageView studentImageView;
    @FXML private TableView<Donation> donationsTable;
    @FXML private TableColumn<Donation, String> donorColumn;
    @FXML private TableColumn<Donation, String> amountColumn;
    @FXML private TableColumn<Donation, String> descriptionColumn;
    @FXML private TableColumn<Donation, String> dateColumn;

    private DonationDAO donationDAO = new DonationDAO();
    private ObservableList<Donation> donations = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        setupTable();
        loadStudentInfo();
        loadDonations();
    }

    private void setupTable() {
        donorColumn.setCellValueFactory(new PropertyValueFactory<>("donorName"));
        donationsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("donationDate"));
        donationsTable.setItems(donations);
    }

    private void loadStudentInfo() {
        SessionManager sessionManager = SessionManager.getInstance();
        Student student = (Student) sessionManager.getCurrentUser();

        welcomeLabel.setText("Welcome, " + student.getName() + "!");
        nameLabel.setText("Name: " + student.getName());
        emailLabel.setText("Email: " + student.getEmail());
        phoneLabel.setText("Phone: " + student.getPhone());
        schoolLabel.setText("School: " + student.getSchool());
        streamLabel.setText("Stream: " + student.getStream().getValue());
        reasonLabel.setText("Reason: " + student.getReasonForRegistration());

        // Load and display student picture
        if (student.getStudentPicture() != null) {
            try {
                ByteArrayInputStream bis = new ByteArrayInputStream(student.getStudentPicture());
                Image image = new Image(bis);
                studentImageView.setImage(image);
                studentImageView.setVisible(true);
            } catch (Exception e) {
                System.err.println("Error loading student picture: " + e.getMessage());
                studentImageView.setVisible(false);
            }
        } else {
            studentImageView.setVisible(false);
        }
    }

    private void loadDonations() {
        SessionManager sessionManager = SessionManager.getInstance();
        Student student = (Student) sessionManager.getCurrentUser();
        donations.clear();
        donations.addAll(donationDAO.getDonationsByStudent(student.getId()));
    }

    @FXML
    private void handleRefresh() {
        loadDonations();
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        Main.showLoginScreen();
    }
}
