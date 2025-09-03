package org.example.donation_application.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.example.donation_application.models.Student;
import java.io.ByteArrayInputStream;

public class StudentDetailsController {

    @FXML private ImageView studentImageView;
    @FXML private Label nameLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private Label schoolLabel;
    @FXML private Label streamLabel;
    @FXML private Label addressLabel;
    @FXML private TextArea reasonTextArea;
    @FXML private Label needAmountLabel;

    public void setStudent(Student student) {
        nameLabel.setText(student.getName());
        emailLabel.setText("Email: " + student.getEmail());
        phoneLabel.setText("Phone: " + student.getPhone());
        schoolLabel.setText("School: " + student.getSchool());
        streamLabel.setText("Stream: " + student.getStream().getValue());
        addressLabel.setText("Address: " + student.getAddress());
        reasonTextArea.setText(student.getReasonForRegistration());
        if(needAmountLabel!= null){
            if(student.getNeedAmount() >= 0){
                needAmountLabel.setText("BDT " + String.format("%,d", student.getNeedAmount()));
            }
            else{
                needAmountLabel.setText("Not Specified");
            }
        }
        // Load student picture
        if (student.getStudentPicture() != null) {
            try {
                ByteArrayInputStream bis = new ByteArrayInputStream(student.getStudentPicture());
                Image image = new Image(bis);
                studentImageView.setImage(image);
            } catch (Exception e) {
                // Use default image
            }
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) nameLabel.getScene().getWindow();
        stage.close();
    }
}
