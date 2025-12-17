package edu.stockton.project.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/** Main JavaFX Application for TSP Art Generator */
public class MainApp extends Application {

  @Override
  public void start(Stage primaryStage) throws Exception {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/main-view.fxml"));
    Parent root = loader.load();

    Scene scene = new Scene(root, 1650, 850);

    primaryStage.setTitle("TSP Art Generator");
    primaryStage.setScene(scene);
    primaryStage.setMinWidth(1600);
    primaryStage.setMinHeight(850);
    primaryStage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
