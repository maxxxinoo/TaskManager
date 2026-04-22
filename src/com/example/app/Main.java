package com.example.app;

import com.example.app.controller.MainController;
import com.example.app.model.User;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import java.awt.*;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/main.fxml")
        );
        Scene scene = new Scene(loader.load(), 1100, 750);
        scene.getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm()
        );

        Image icon = new Image(getClass().getResourceAsStream("/images/app_icon.png"));
        stage.getIcons().add(icon);


        stage.setTitle("Task Manager");
        stage.setScene(scene);

        MainController controller = loader.getController();

        stage.show();
        Platform.runLater(() -> controller.setUser(null));
    }

    public static void main(String[] args) {
        launch();
    }
}