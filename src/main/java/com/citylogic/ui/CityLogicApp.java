package com.citylogic.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main JavaFX Application launcher for CityLogic.
 */
public class CityLogicApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/citylogic/ui/GameView.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1280, 820);
            scene.getStylesheets().add(getClass().getResource("/com/citylogic/ui/styles.css").toExternalForm());

            primaryStage.setTitle("CityLogic — Municipal Simulation & Spatial Planner (JavaFX)");
            primaryStage.setMinWidth(1080);
            primaryStage.setMinHeight(720);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to start CityLogic JavaFX Application: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
