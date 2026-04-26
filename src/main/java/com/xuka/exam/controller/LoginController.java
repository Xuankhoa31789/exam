package com.xuka.exam.controller;

import java.io.IOException;

import com.xuka.exam.ExamApplication;
import com.xuka.exam.dao.UserAccountDAO;
import com.xuka.exam.dao.UserInfoDAO;
import com.xuka.exam.models.UserAccount;
import com.xuka.exam.models.UserInfo;
import com.xuka.exam.util.PasswordUtil;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller for login screen
 * Handles user authentication using UserAccount model
 */
public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    /**
     * Handle login button action
     * Validates username and password against database
     */
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

        // Query database for user account with this username
        UserAccountDAO userAccountDAO = new UserAccountDAO();
        UserAccount userAccount = userAccountDAO.getByUsername(username);

        // Check if user exists
        if (userAccount == null) {
            messageLabel.setText("Invalid username or password.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        // Verify password using salt and hash
        if (!PasswordUtil.verifyPassword(password, userAccount.getSalt(), userAccount.getPwdHash())) {
            messageLabel.setText("Invalid username or password.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        // Get user info
        UserInfoDAO userInfoDAO = new UserInfoDAO();
        UserInfo userInfo = userInfoDAO.getById(userAccount.getUcId());

        if (userInfo == null) {
            messageLabel.setText("User information not found.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        // Login successful
        String roleName = userAccount.getRole() == 1 ? "Teacher" : "Student";
        messageLabel.setText("Login successful! Welcome " + userInfo.getFullName() + " (" + roleName + ")");
        messageLabel.setStyle("-fx-text-fill: green;");
        System.out.println("User " + username + " logged in successfully as " + roleName);

        // Navigate to appropriate dashboard based on role
        try {
            if (userAccount.getRole() == 1) {
                // Teacher dashboard
                FXMLLoader fxmlLoader = new FXMLLoader(ExamApplication.class.getResource("teacher_dashboard.fxml"));
                Scene scene = new Scene(fxmlLoader.load(), 1400, 900);
                TeacherDashboardController controller = fxmlLoader.getController();
                controller.setCurrentTeacherId(userInfo.getUcInfoId());
                controller.setWelcomeMessage(userInfo.getFullName());
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setTitle("Teacher Dashboard - " + userInfo.getFullName());
                stage.setScene(scene);
                stage.setMaximized(true);
            } else {
                // Student dashboard
                FXMLLoader fxmlLoader = new FXMLLoader(ExamApplication.class.getResource("student_dashboard.fxml"));
                Scene scene = new Scene(fxmlLoader.load(), 1400, 900);
                StudentDashboardController controller = fxmlLoader.getController();
                controller.setCurrentStudentId(userInfo.getUcInfoId());
                controller.setWelcomeMessage(userInfo.getFullName());
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setTitle("Student Dashboard - " + userInfo.getFullName());
                stage.setScene(scene);
                stage.setMaximized(true);
            }
        } catch (IOException e) {
            e.printStackTrace();
            messageLabel.setText("Error loading dashboard.");
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }

    /**
     * Handle clear button action
     */
    @FXML
    private void onClearButtonAction() {
        usernameField.clear();
        passwordField.clear();
        messageLabel.setText("");
    }

    /**
     * Navigate to register screen
     */
    @FXML
    private void onRegisterButtonAction() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(ExamApplication.class.getResource("register_screen.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 500, 600);
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

