package org.example.donation_application.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import org.example.donation_application.models.Donor;

import java.io.ByteArrayInputStream;
import java.util.function.Consumer;

public class DonorCardController {

    @FXML private ImageView donorImageView;
    @FXML private VBox root;
    @FXML private Label nameLabel;
    @FXML private Label orgLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private Label statusLabel;

    private Donor donor;
    private Consumer<Donor> onCardClickCallback;
    private Consumer<Donor> onDetailViewCallback;
    private boolean isSelected = false;

    public DonorCardController() {
        // Empty constructor required by FXML
    }

    @FXML
    private void initialize() {
        try {
            setupHoverEffects();
        } catch (Exception e) {
            System.err.println("Error in DonorCardController initialize: " + e.getMessage());
        }
    }

    private void setupHoverEffects() {
        if (root != null) {
            root.setOnMouseEntered(e -> {
                if (!isSelected) {
                    root.getStyleClass().removeAll("donor-card-selected");
                    root.getStyleClass().add("donor-card-hover");
                }
            });

            root.setOnMouseExited(e -> {
                if (!isSelected) {
                    root.getStyleClass().removeAll("donor-card-hover");
                }
            });
        }
    }

    public void setDonor(Donor donor) {
        this.donor = donor;
        updateCardDisplay();
    }

    public void setOnCardClickCallback(Consumer<Donor> callback) {
        this.onCardClickCallback = callback;
    }

    public void setOnDetailViewCallback(Consumer<Donor> callback) {
        this.onDetailViewCallback = callback;
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
        updateSelectionStyle();
    }

    public boolean isSelected() {
        return isSelected;
    }

    private void updateSelectionStyle() {
        if (root != null) {
            root.getStyleClass().removeAll("donor-card-selected", "donor-card-hover");

            if (isSelected) {
                root.getStyleClass().add("donor-card-selected");
            }
        }
    }
    private void loadDonorImage() {
        try {
            if (donorImageView != null && donor.getDonorPicture() != null && donor.getDonorPicture().length > 0) {
                ByteArrayInputStream bis = new ByteArrayInputStream(donor.getDonorPicture());
                Image image = new Image(bis);
                donorImageView.setImage(image);
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
            if (donorImageView != null) {
                donorImageView.setImage(null);
                donorImageView.setStyle("-fx-background-color: #e0e0e0; -fx-background-radius: 50;");
            }
        } catch (Exception e) {
            // Ignore styling errors
        }
    }
    private void updateCardDisplay() {
        try {
            if (donor == null) return;

            if (nameLabel != null) {
                nameLabel.setText(donor.getName());
            }
            if (orgLabel != null) {
                orgLabel.setText(donor.getOrganization());
            }
            if (emailLabel != null) {
                emailLabel.setText("Email: " + donor.getEmail());
            }
            if (phoneLabel != null) {
                phoneLabel.setText("Phone: " + donor.getPhone());
            }

            // Status label is hidden for donors (no pending state)
            if (statusLabel != null) {
                statusLabel.setVisible(false);
            }
            loadDonorImage();

        } catch (Exception e) {
            System.err.println("Error updating donor card display: " + e.getMessage());
        }
    }

    @FXML
    private void handleCardClick(MouseEvent event) {
        try {
            if (event.getClickCount() == 2) {
                // Double-click for details
                if (donor != null && onDetailViewCallback != null) {
                    onDetailViewCallback.accept(donor);
                }
            } else {
                // Single-click for selection
                if (donor != null && onCardClickCallback != null) {
                    onCardClickCallback.accept(donor);
                }
            }
        } catch (Exception e) {
            System.err.println("Error handling donor card click: " + e.getMessage());
        }
    }

    public Donor getDonor() {
        return donor;
    }

    public VBox getRoot() {
        return root;
    }
}
