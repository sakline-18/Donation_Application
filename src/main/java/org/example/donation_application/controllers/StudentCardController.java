package org.example.donation_application.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import org.example.donation_application.models.Student;
import java.io.ByteArrayInputStream;
import java.util.function.Consumer;

public class StudentCardController {

    @FXML private VBox root;
    @FXML private ImageView studentImageView;
    @FXML private Label nameLabel;
    @FXML private Label schoolLabel;
    @FXML private Label streamLabel;
    @FXML private Label statusLabel;

    private Student student;
    private Consumer<Student> onCardClickCallback;
    private boolean isSelected = false;

    @FXML
    private void initialize() {
        try {
            if (studentImageView != null) {
                setDefaultImage();
            }
            // Add hover effects
            setupHoverEffects();
        } catch (Exception e) {
            System.err.println("Error in StudentCardController initialize: " + e.getMessage());
        }
    }

    /**
     * Sets up hover effects for the card
     */
    private void setupHoverEffects() {
        if (root != null) {
            root.setOnMouseEntered(e -> {
                if (!isSelected) {
                    root.getStyleClass().removeAll("student-card-selected");
                    root.getStyleClass().add("student-card-hover");
                }
            });

            root.setOnMouseExited(e -> {
                if (!isSelected) {
                    root.getStyleClass().removeAll("student-card-hover");
                }
            });
        }
    }

    /**
     * Sets the student data and updates the card display
     */
    public void setStudent(Student student) {
        this.student = student;
        updateCardDisplay();
    }

    /**
     * Sets the callback for card click events
     */
    public void setOnCardClickCallback(Consumer<Student> callback) {
        this.onCardClickCallback = callback;
    }

    /**
     * Updates the visual selection state of the card
     */
    public void setSelected(boolean selected) {
        this.isSelected = selected;
        updateSelectionStyle();
    }

    /**
     * Gets the current selection state
     */
    public boolean isSelected() {
        return isSelected;
    }

    /**
     * Updates the card's visual style based on selection state
     */
    private void updateSelectionStyle() {
        if (root != null) {
            // Remove all selection-related style classes
            root.getStyleClass().removeAll("student-card-selected", "student-card-hover");

            // Add appropriate style class
            if (isSelected) {
                root.getStyleClass().add("student-card-selected");
            }
        }
    }

    /**
     * Updates the card display with student information
     */
    private void updateCardDisplay() {
        try {
            if (student == null) return;

            if (nameLabel != null) {
                nameLabel.setText(student.getName());
            }
            if (schoolLabel != null) {
                schoolLabel.setText(student.getSchool());
            }
            if (streamLabel != null) {
                streamLabel.setText(student.getStream().getValue());
            }

            // Handle status for pending students
            if (statusLabel != null) {
                if (student.isPending()) {
                    statusLabel.setText("Pending Approval");
                    statusLabel.setVisible(true);
                } else {
                    statusLabel.setVisible(false);
                }
            }

            // Load student image
            loadStudentImage();

        } catch (Exception e) {
            System.err.println("Error updating card display: " + e.getMessage());
        }
    }

    /**
     * Handles card click events
     */
//    @FXML
//    private void handleCardClick(MouseEvent event) {
//        try {
//            if (student != null && onCardClickCallback != null) {
//                System.out.println("Card clicked: " + student.getName());
//                onCardClickCallback.accept(student);
//            }
//        } catch (Exception e) {
//            System.err.println("Error handling card click: " + e.getMessage());
//        }
//    }

    // ... (keep your existing loadStudentImage(), setDefaultImage(), and getStudent() methods)

    private void loadStudentImage() {
        try {
            if (studentImageView != null && student.getStudentPicture() != null && student.getStudentPicture().length > 0) {
                ByteArrayInputStream bis = new ByteArrayInputStream(student.getStudentPicture());
                Image image = new Image(bis);
                studentImageView.setImage(image);
            } else {
                setDefaultImage();
            }
        } catch (Exception e) {
            System.err.println("Error loading student image: " + e.getMessage());
            setDefaultImage();
        }
    }

    private void setDefaultImage() {
        try {
            if (studentImageView != null) {
                studentImageView.setImage(null);
                studentImageView.setStyle("-fx-background-color: #e0e0e0; -fx-background-radius: 50;");
            }
        } catch (Exception e) {
            // Ignore styling errors
        }
    }

    public Student getStudent() {
        return student;
    }
    private Consumer<Student> onDetailViewCallback;

    public void setOnDetailViewCallback(Consumer<Student> callback) {
        this.onDetailViewCallback = callback;
    }

    @FXML
    private void handleCardClick(MouseEvent event) {
        if (event.getClickCount() == 2) { // Double-click for details
            if (student != null && onDetailViewCallback != null) {
                onDetailViewCallback.accept(student);
            }
        } else { // Single click for selection
            if (student != null && onCardClickCallback != null) {
                onCardClickCallback.accept(student);
            }
        }
    }
}
