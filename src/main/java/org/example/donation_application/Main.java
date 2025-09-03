package org.example.donation_application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        primaryStage.setTitle("Donation Portal");
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(400);
        primaryStage.setMinHeight(500);
        showLoginScreen();
    }

    public static void showLoginScreen() {
        System.out.println("Loading login screen...");
        loadScene("login.fxml", "Donation Portal - Login");
    }

    public static void showScene(String fxmlFile, String title) {
        System.out.println("Loading scene: " + fxmlFile);
        loadScene(fxmlFile, title);
    }

    private static void loadScene(String fxmlFile, String title) {
        try {
            System.out.println("Attempting to load: " + fxmlFile);
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource(fxmlFile));
            Scene scene = new Scene(fxmlLoader.load(), 1_000, 700);

            // Load CSS stylesheet
            String css = Main.class.getResource("styles/login.css").toExternalForm();
            scene.getStylesheets().add(css);

            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            primaryStage.show();
            System.out.println("Successfully loaded: " + fxmlFile);
        } catch (IOException e) {
            System.err.println("Error loading FXML file: " + fxmlFile);
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        launch();
    }
}
