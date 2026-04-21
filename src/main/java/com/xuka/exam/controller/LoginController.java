package com.xuka.exam.controller;

import java.io.IOException;

import com.xuka.exam.ExamApplication;
import com.xuka.exam.dao.StudentDAO;
import com.xuka.exam.models.Student;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    @FXML
    private void onLoginButtonAction() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Basic validation
        if (username == null || username.isEmpty() ||
            password == null || password.isEmpty()) {
            messageLabel.setText("Please fill in all fields.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        // Query database for student with this username
        StudentDAO dao = new StudentDAO();
        Student student = dao.getByUsername(username);

        // Authenticate
        if (student == null) {
            messageLabel.setText("Invalid username or password.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        if (!student.getPassword().equals(password)) {
            messageLabel.setText("Invalid username or password.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        // Login successful
        messageLabel.setText("Login successful! Welcome " + student.getFullName());
        messageLabel.setStyle("-fx-text-fill: green;");
        System.out.println("User " + username + " logged in successfully.");
    }

    @FXML
    private void onClearButtonAction() {
        usernameField.clear();
        passwordField.clear();
        messageLabel.setText("");
    }

    @FXML
    private void onRegisterButtonAction() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(ExamApplication.class.getResource("register_screen.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 400, 500);
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setTitle("Register");
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            messageLabel.setText("Error loading register screen.");
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }
}

