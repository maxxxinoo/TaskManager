package com.example.app.service;

import com.example.app.model.User;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.util.Duration;

public class UserService {



    private void shakeField(Node field) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(100), field);
        shake.setFromX(0);
        shake.setToX(10);
        shake.setCycleCount(4);
        shake.setAutoReverse(true);
        shake.play();

        shake.setOnFinished(event -> field.setTranslateX(0));
    }




}