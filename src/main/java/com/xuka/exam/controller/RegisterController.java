package com.xuka.exam.controller;

import com.xuka.exam.dao.StudentDAO;
import com.xuka.exam.models.Student;
import com.xuka.exam.ExamApplication;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.stream.IntStream;

public class RegisterController implements Initializable {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private ComboBox<Integer> dayComboBox;

    @FXML
    private ComboBox<Integer> monthComboBox;

    @FXML
    private ComboBox<Integer> yearComboBox;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Button registerButton;

    @FXML
    private Button backButton;

    @FXML
    private Label messageLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Populate day (1-31)
        dayComboBox.getItems().addAll(IntStream.rangeClosed(1, 31).boxed().toList());
        // Populate month (1-12)
        monthComboBox.getItems().addAll(IntStream.rangeClosed(1, 12).boxed().toList());
        // Populate year (1950 to current year)
        int currentYear = LocalDate.now().getYear();
        yearComboBox.getItems().addAll(IntStream.rangeClosed(1950, currentYear).boxed().toList());
    }

    @FXML
    private void onRegisterButtonAction() {
        String username = usernameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        Integer day = dayComboBox.getValue();
        Integer month = monthComboBox.getValue();
        Integer year = yearComboBox.getValue();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        LocalDate dateOfBirth = null;
        if (day != null && month != null && year != null) {
            dateOfBirth = LocalDate.of(year, month, day);
        }

        // Basic validation
        if (username == null || username.isEmpty() ||
            email == null || email.isEmpty() ||
            password == null || password.isEmpty() ||
            confirmPassword == null || confirmPassword.isEmpty() ||
            dateOfBirth == null) {
            messageLabel.setText("Please fill in all required fields.");
            messageLabel.setStyle("-fx-text-fill: red;");
            System.out.println("Please fill in all required fields.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            messageLabel.setText("Passwords do not match.");
            messageLabel.setStyle("-fx-text-fill: red;");
            System.out.println("Passwords do not match.");
            return;
        }

        // Create Student object (fullName = username for demo, department left blank)
        Student student = new Student(
            username, // fullName
            email,
            phone,
            dateOfBirth,
            "", // department (not in form)
            username,
            password
        );

        StudentDAO dao = new StudentDAO();
        boolean success = dao.save(student);
        if (success) {
            messageLabel.setText("Registration successful!");
            messageLabel.setStyle("-fx-text-fill: green;");
            System.out.println("Registration successful!");
            // Clear fields on success
            usernameField.clear();
            emailField.clear();
            phoneField.clear();
            dayComboBox.setValue(null);
            monthComboBox.setValue(null);
            yearComboBox.setValue(null);
            passwordField.clear();
            confirmPasswordField.clear();
        } else {
            messageLabel.setText("Registration failed. Student may already exist or there was a DB error.");
            messageLabel.setStyle("-fx-text-fill: red;");
            System.out.println("Registration failed. Student may already exist or there was a DB error.");
        }
    }

    @FXML
    private void onUsernameAction() {
        // Move to next field (email)
        emailField.requestFocus();
    }

    @FXML
    private void onEmailAction() {
        // Move to next field (phone)
        phoneField.requestFocus();
    }

    @FXML
    private void onPhoneAction() {
        // Move to next field (day combo box)
        dayComboBox.requestFocus();
    }

    @FXML
    private void onPasswordAction() {
        // Move to next field (confirm password)
        confirmPasswordField.requestFocus();
    }

    @FXML
    private void onConfirmPasswordAction() {
        // Trigger registration when Enter is pressed
        onRegisterButtonAction();
    }

    @FXML
    private void onBackButtonAction() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(ExamApplication.class.getResource("login_screen.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 400, 500);
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setTitle("Login");
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            messageLabel.setText("Error loading login screen.");
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }
}
