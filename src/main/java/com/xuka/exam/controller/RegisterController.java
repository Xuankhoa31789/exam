package com.xuka.exam.controller;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.stream.IntStream;

import com.xuka.exam.ExamApplication;
import com.xuka.exam.dao.UserAccountDAO;
import com.xuka.exam.dao.UserInfoDAO;
import com.xuka.exam.models.UserAccount;
import com.xuka.exam.models.UserInfo;
import com.xuka.exam.util.PasswordUtil;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

/**
 * Controller for user registration screen
 * Handles registration for both students and teachers
 */
public class RegisterController implements Initializable {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private TextField departmentField;

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
    private RadioButton studentRadioButton;

    @FXML
    private RadioButton teacherRadioButton;

    @FXML
    private ToggleGroup roleToggleGroup;

    @FXML
    private Button registerButton;

    @FXML
    private Button backButton;

    @FXML
    private Label messageLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize date combo boxes
        dayComboBox.getItems().addAll(IntStream.rangeClosed(1, 31).boxed().toList());
        monthComboBox.getItems().addAll(IntStream.rangeClosed(1, 12).boxed().toList());
        int currentYear = LocalDate.now().getYear();
        yearComboBox.getItems().addAll(IntStream.rangeClosed(1950, currentYear).boxed().toList());

        // Initialize role toggle group
        roleToggleGroup = new ToggleGroup();
        studentRadioButton.setToggleGroup(roleToggleGroup);
        teacherRadioButton.setToggleGroup(roleToggleGroup);
        studentRadioButton.setSelected(true); // Default to student
    }

    /**
     * Handle registration button action
     */
    @FXML
    private void onRegisterButtonAction() {
        String username = usernameField.getText();
        String fullName = fullNameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String department = departmentField.getText();
        Integer day = dayComboBox.getValue();
        Integer month = monthComboBox.getValue();
        Integer year = yearComboBox.getValue();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        LocalDate dateOfBirth = null;

        if (day != null && month != null && year != null) {
            dateOfBirth = LocalDate.of(year, month, day);
        }

        // Validate all fields
        if (username == null || username.isEmpty() ||
            fullName == null || fullName.isEmpty() ||
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

        if (password.length() < 6) {
            messageLabel.setText("Password must be at least 6 characters long.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        // Get selected role (0 = Student, 1 = Teacher)
        int role = studentRadioButton.isSelected() ? 0 : 1;

        try {
            // Generate salt and hash password
            String salt = PasswordUtil.generateSalt();
            String pwdHash = PasswordUtil.hashPassword(password, salt);

            // Create UserAccount
            UserAccount userAccount = new UserAccount(username, pwdHash, salt, role);
            UserAccountDAO userAccountDAO = new UserAccountDAO();
            boolean accountSaved = userAccountDAO.save(userAccount);

            if (!accountSaved) {
                messageLabel.setText("Registration failed. Username may already exist.");
                messageLabel.setStyle("-fx-text-fill: red;");
                System.out.println("Failed to save user account.");
                return;
            }

            // Retrieve the saved account to get the ucId
            UserAccount savedAccount = userAccountDAO.getByUsername(username);
            if (savedAccount == null) {
                messageLabel.setText("Registration failed. Could not retrieve account.");
                messageLabel.setStyle("-fx-text-fill: red;");
                return;
            }

            // Create UserInfo
            UserInfo userInfo = new UserInfo(
                null, // code will be generated or left null
                fullName,
                email,
                phone,
                dateOfBirth,
                department,
                savedAccount
            );

            UserInfoDAO userInfoDAO = new UserInfoDAO();
            boolean infoSaved = userInfoDAO.save(userInfo);

            if (infoSaved) {
                messageLabel.setText("Registration successful!");
                messageLabel.setStyle("-fx-text-fill: green;");
                System.out.println("Registration successful!");

                // Clear fields on success
                clearAllFields();

                // Navigate back to login after a short delay
                new Thread(() -> {
                    try {
                        Thread.sleep(1500);
                        javafx.application.Platform.runLater(this::onBackButtonAction);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            } else {
                messageLabel.setText("Registration failed. Could not save user information.");
                messageLabel.setStyle("-fx-text-fill: red;");
                System.out.println("Failed to save user info.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Registration error: " + e.getMessage());
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }

    /**
     * Clear all input fields
     */
    private void clearAllFields() {
        usernameField.clear();
        fullNameField.clear();
        emailField.clear();
        phoneField.clear();
        departmentField.clear();
        dayComboBox.setValue(null);
        monthComboBox.setValue(null);
        yearComboBox.setValue(null);
        passwordField.clear();
        confirmPasswordField.clear();
        studentRadioButton.setSelected(true);
    }

    @FXML
    private void onUsernameAction() {
        fullNameField.requestFocus();
    }

    @FXML
    private void onFullNameAction() {
        emailField.requestFocus();
    }

    @FXML
    private void onEmailAction() {
        phoneField.requestFocus();
    }

    @FXML
    private void onPhoneAction() {
        departmentField.requestFocus();
    }

    @FXML
    private void onDepartmentAction() {
        dayComboBox.requestFocus();
    }

    @FXML
    private void onPasswordAction() {
        confirmPasswordField.requestFocus();
    }

    @FXML
    private void onConfirmPasswordAction() {
        onRegisterButtonAction();
    }

    /**
     * Navigate back to login screen
     */
    @FXML
    private void onBackButtonAction() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(ExamApplication.class.getResource("login_screen.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 500, 400);
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
