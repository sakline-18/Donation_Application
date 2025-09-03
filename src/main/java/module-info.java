module org.example.donation_application {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;
    requires itextpdf;

    opens org.example.donation_application to javafx.fxml;
    opens org.example.donation_application.controllers to javafx.fxml;
    opens org.example.donation_application.models to javafx.base;

    exports org.example.donation_application;
    exports org.example.donation_application.controllers;
    exports org.example.donation_application.models;
}
