package edu.stockton.project.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

  @Override
  public void start(Stage stage) throws Exception {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/main-view.fxml"));

    Parent root = fxmlLoader.load();

    Scene scene = new Scene(root, 1200, 700);

    stage.setTitle("TSP Art Generator");
    stage.setScene(scene);
    stage.setMinWidth(1000);
    stage.setMinHeight(600);
    stage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
