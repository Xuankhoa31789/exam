package com.xuka.exam.controller;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import com.xuka.exam.ExamApplication;
import com.xuka.exam.dao.ExamAttemptDAO;
import com.xuka.exam.dao.ExamDAO;
import com.xuka.exam.dao.UserAccountDAO;
import com.xuka.exam.dao.UserInfoDAO;
import com.xuka.exam.models.Exam;
import com.xuka.exam.models.ExamAttempt;
import com.xuka.exam.models.Subject;
import com.xuka.exam.models.UserAccount;
import com.xuka.exam.models.UserInfo;
import com.xuka.exam.util.PasswordUtil;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controller for Teacher Dashboard
 * Displays exam statistics and student performance data
 */
public class TeacherDashboardController implements Initializable {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label dateTimeLabel;

    @FXML
    private Label totalExamsLabel;

    @FXML
    private Label completedExamsLabel;

    @FXML
    private Label scheduledExamsLabel;

    @FXML
    private Label avgScoreLabel;

    @FXML
    private Label activeStudentsLabel;

    @FXML
    private PieChart examStatusChart;

    @FXML
    private BarChart<String, Number> performanceChart;

    @FXML
    private TableView<ExamAttemptRow> attemptsTable;

    @FXML
    private TableColumn<ExamAttemptRow, String> studentNameColumn;

    @FXML
    private TableColumn<ExamAttemptRow, String> examNameColumn;

    @FXML
    private TableColumn<ExamAttemptRow, String> startTimeColumn;

    @FXML
    private TableColumn<ExamAttemptRow, String> endTimeColumn;

    @FXML
    private TableColumn<ExamAttemptRow, Integer> scoreColumn;

    @FXML
    private TableColumn<ExamAttemptRow, String> statusColumn;

    @FXML
    private TableColumn<ExamAttemptRow, String> durationColumn;

    private ExamDAO examDAO;
    private ExamAttemptDAO examAttemptDAO;
    private UserInfoDAO userInfoDAO;
    private UserAccountDAO userAccountDAO;
    private int currentTeacherId = 1; // This should be set from login session

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        examDAO = new ExamDAO();
        examAttemptDAO = new ExamAttemptDAO();
        userInfoDAO = new UserInfoDAO();
        userAccountDAO = new UserAccountDAO();

        // Initialize table columns
        initializeTableColumns();

        // Load dashboard data
        loadDashboardData();

        // Update date-time label
        updateDateTime();
    }

    /**
     * Initialize table columns with property values
     */
    private void initializeTableColumns() {
        studentNameColumn.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        examNameColumn.setCellValueFactory(new PropertyValueFactory<>("examName"));
        startTimeColumn.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        endTimeColumn.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        scoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));

        // Color code status
        statusColumn.setCellFactory(col -> new TableCell<ExamAttemptRow, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Completed".equals(item)) {
                        setStyle("-fx-text-fill: #388e3c; -fx-font-weight: bold;");
                    } else if ("In Progress".equals(item)) {
                        setStyle("-fx-text-fill: #f57c00; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // Highlight high scores
        scoreColumn.setCellFactory(col -> new TableCell<ExamAttemptRow, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    if (item >= 80) {
                        setStyle("-fx-text-fill: #388e3c; -fx-font-weight: bold;");
                    } else if (item >= 60) {
                        setStyle("-fx-text-fill: #f57c00; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
                    }
                }
            }
        });
    }

    /**
     * Load all dashboard data
     */
    private void loadDashboardData() {
        // Load in background thread to avoid UI freezing
        new Thread(() -> {
            try {
                // Fetch data from DAO
                List<Exam> allExams = examDAO.getByTeacher(currentTeacherId);
                long totalExams = examDAO.countByTeacher(currentTeacherId);
                long completedExams = examDAO.countByTeacherAndStatus(currentTeacherId, "Completed");
                long scheduledExams = examDAO.countByTeacherAndStatus(currentTeacherId, "Scheduled");
                double avgScore = examAttemptDAO.getAverageScoreByTeacher(currentTeacherId);
                long activeStudents = examAttemptDAO.getUniqueStudentsCountByTeacher(currentTeacherId);
                List<ExamAttempt> recentAttempts = examAttemptDAO.getRecentAttemptsByTeacher(currentTeacherId, 10);

                // Update UI in main thread
                Platform.runLater(() -> {
                    updateSummaryCards(totalExams, completedExams, scheduledExams, avgScore, activeStudents);
                    updateExamStatusChart(allExams);
                    updatePerformanceChart(recentAttempts);
                    updateAttemptsTable(recentAttempts);
                });
            } catch (Exception e) {
                System.err.println("Error loading dashboard data: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Update summary cards with statistics
     */
    private void updateSummaryCards(long total, long completed, long scheduled, double avgScore, long activeStudents) {
        totalExamsLabel.setText(String.valueOf(total));
        completedExamsLabel.setText(String.valueOf(completed));
        scheduledExamsLabel.setText(String.valueOf(scheduled));
        avgScoreLabel.setText(String.format("%.1f", avgScore));
        activeStudentsLabel.setText(String.valueOf(activeStudents));
    }

    /**
     * Update pie chart showing exam status distribution
     */
    private void updateExamStatusChart(List<Exam> exams) {
        Map<String, Integer> statusCount = new HashMap<>();
        statusCount.put("Completed", 0);
        statusCount.put("Scheduled", 0);
        statusCount.put("Ongoing", 0);

        for (Exam exam : exams) {
            String status = exam.getStatus() != null ? exam.getStatus() : "Scheduled";
            statusCount.put(status, statusCount.getOrDefault(status, 0) + 1);
        }

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        statusCount.forEach((status, count) -> {
            if (count > 0) {
                pieData.add(new PieChart.Data(status + " (" + count + ")", count));
            }
        });

        examStatusChart.setData(pieData);
        examStatusChart.setTitle("Exam Status Distribution");

        // Add colors to pie slices
        int[] colors = {0x388e3c, 0xf57c00, 0xd32f2f};
        int index = 0;
        for (PieChart.Data data : pieData) {
            String color = String.format("#%06x", colors[index % colors.length]);
            data.getNode().setStyle("-fx-pie-color: " + color + ";");
            index++;
        }
    }

    /**
     * Update bar chart showing recent exam performance
     */
    private void updatePerformanceChart(List<ExamAttempt> attempts) {
        Map<String, List<Integer>> examScores = new LinkedHashMap<>();

        for (ExamAttempt attempt : attempts) {
            if ("Completed".equals(attempt.getStatus())) {
                String examName = attempt.getExam().getExamTitle();
                examScores.computeIfAbsent(examName, k -> new ArrayList<>()).add(attempt.getScore());
            }
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Average Score");

        examScores.forEach((examName, scores) -> {
            double avg = scores.stream().mapToInt(Integer::intValue).average().orElse(0);
            series.getData().add(new XYChart.Data<>(
                examName.length() > 15 ? examName.substring(0, 15) + "..." : examName,
                avg
            ));
        });

        performanceChart.getData().clear();
        performanceChart.getData().add(series);
        performanceChart.setTitle("Recent Exam Performance - Average Scores");
    }

    /**
     * Update table with recent attempts
     */
    private void updateAttemptsTable(List<ExamAttempt> attempts) {
        ObservableList<ExamAttemptRow> rows = FXCollections.observableArrayList();

        for (ExamAttempt attempt : attempts) {
            ExamAttemptRow row = new ExamAttemptRow(
                attempt.getStudent().getFullName(),
                attempt.getExam().getExamTitle(),
                formatDateTime(attempt.getStartTime()),
                attempt.getEndTime() != null ? formatDateTime(attempt.getEndTime()) : "N/A",
                attempt.getScore(),
                attempt.getStatus(),
                calculateDuration(attempt)
            );
            rows.add(row);
        }

        attemptsTable.setItems(rows);
    }

    /**
     * Format LocalDateTime to readable string
     */
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "N/A";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return dateTime.format(formatter);
    }

    /**
     * Calculate exam duration in minutes
     */
    private String calculateDuration(ExamAttempt attempt) {
        if (attempt.getStartTime() == null || attempt.getEndTime() == null) {
            return "N/A";
        }
        long minutes = java.time.temporal.ChronoUnit.MINUTES.between(
            attempt.getStartTime(),
            attempt.getEndTime()
        );
        return minutes + " min";
    }

    /**
     * Update date-time label with current date and time
     */
    private void updateDateTime() {
        new Thread(() -> {
            while (true) {
                Platform.runLater(() -> {
                    LocalDateTime now = LocalDateTime.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy 'at' HH:mm:ss");
                    dateTimeLabel.setText(now.format(formatter));
                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }).start();
    }

    /**
     * Set the current teacher ID (called from login controller)
     */
    public void setCurrentTeacherId(int teacherId) {
        this.currentTeacherId = teacherId;
        loadDashboardData();
    }

    /**
     * Set welcome message with teacher name (optional)
     */
    public void setWelcomeMessage(String teacherName) {
        welcomeLabel.setText("Welcome, " + teacherName + " - Dashboard");
    }

    @FXML
    public void onProfileMenuAction() {
        UserInfo teacherInfo = getCurrentTeacherInfo();
        if (teacherInfo == null) {
            showAlert(Alert.AlertType.ERROR, "Profile unavailable", "Could not load teacher profile.");
            return;
        }

        UserAccount account = teacherInfo.getUserAccount();
        GridPane profileGrid = createFormGrid();
        addReadOnlyRow(profileGrid, 0, "Username", account == null ? "N/A" : account.getUsername());
        addReadOnlyRow(profileGrid, 1, "Full Name", teacherInfo.getFullName());
        addReadOnlyRow(profileGrid, 2, "Email", teacherInfo.getEmail());
        addReadOnlyRow(profileGrid, 3, "Phone", teacherInfo.getPhone());
        addReadOnlyRow(profileGrid, 4, "Date of Birth", teacherInfo.getDateOfBirth() == null ? "N/A" : teacherInfo.getDateOfBirth().toString());
        addReadOnlyRow(profileGrid, 5, "Department", teacherInfo.getDepartment());
        addReadOnlyRow(profileGrid, 6, "Role", "Teacher");

        showContentWindow("Profile", profileGrid, 420, 330);
    }

    @FXML
    public void onEditProfileMenuAction() {
        UserInfo teacherInfo = getCurrentTeacherInfo();
        if (teacherInfo == null || teacherInfo.getUserAccount() == null) {
            showAlert(Alert.AlertType.ERROR, "Edit unavailable", "Could not load teacher account.");
            return;
        }

        UserAccount account = teacherInfo.getUserAccount();
        TextField usernameField = new TextField(account.getUsername());
        TextField fullNameField = new TextField(teacherInfo.getFullName());
        TextField emailField = new TextField(teacherInfo.getEmail());
        TextField phoneField = new TextField(teacherInfo.getPhone() == null ? "" : teacherInfo.getPhone());
        TextField departmentField = new TextField(teacherInfo.getDepartment() == null ? "" : teacherInfo.getDepartment());
        DatePicker dobPicker = new DatePicker(teacherInfo.getDateOfBirth());

        GridPane form = createFormGrid();
        addEditableRow(form, 0, "Username", usernameField);
        addEditableRow(form, 1, "Full Name", fullNameField);
        addEditableRow(form, 2, "Email", emailField);
        addEditableRow(form, 3, "Phone", phoneField);
        addEditableRow(form, 4, "Date of Birth", dobPicker);
        addEditableRow(form, 5, "Department", departmentField);

        Stage stage = createModalStage("Edit Profile");
        Button saveButton = new Button("Save");
        saveButton.setDefaultButton(true);
        saveButton.setOnAction(event -> {
            String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
            String fullName = fullNameField.getText() == null ? "" : fullNameField.getText().trim();
            String email = emailField.getText() == null ? "" : emailField.getText().trim();

            if (username.isEmpty() || fullName.isEmpty() || email.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Missing fields", "Username, full name, and email are required.");
                return;
            }

            UserAccount existingAccount = userAccountDAO.getByUsername(username);
            if (existingAccount != null && existingAccount.getUcId() != account.getUcId()) {
                showAlert(Alert.AlertType.WARNING, "Username exists", "Please choose another username.");
                return;
            }

            account.setUsername(username);
            teacherInfo.setFullName(fullName);
            teacherInfo.setEmail(email);
            teacherInfo.setPhone(phoneField.getText());
            teacherInfo.setDateOfBirth(dobPicker.getValue());
            teacherInfo.setDepartment(departmentField.getText());

            boolean accountUpdated = userAccountDAO.update(account);
            boolean infoUpdated = userInfoDAO.update(teacherInfo);
            if (!accountUpdated || !infoUpdated) {
                showAlert(Alert.AlertType.ERROR, "Save failed", "Could not update profile.");
                return;
            }

            setWelcomeMessage(fullName);
            showAlert(Alert.AlertType.INFORMATION, "Profile updated", "Your profile was updated.");
            stage.close();
        });

        Button closeButton = new Button("Close");
        closeButton.setCancelButton(true);
        closeButton.setOnAction(event -> stage.close());
        HBox actions = new HBox(10, saveButton, closeButton);
        actions.setStyle("-fx-alignment: center-right;");

        VBox content = new VBox(12, form, actions);
        content.setPadding(new Insets(18));
        stage.setScene(new Scene(content, 460, 360));
        stage.showAndWait();
    }

    @FXML
    public void onChangePasswordMenuAction() {
        UserInfo teacherInfo = getCurrentTeacherInfo();
        if (teacherInfo == null || teacherInfo.getUserAccount() == null) {
            showAlert(Alert.AlertType.ERROR, "Password unavailable", "Could not load teacher account.");
            return;
        }

        UserAccount account = teacherInfo.getUserAccount();
        PasswordField currentPasswordField = new PasswordField();
        PasswordField newPasswordField = new PasswordField();
        PasswordField confirmPasswordField = new PasswordField();
        currentPasswordField.setPromptText("Current password");
        newPasswordField.setPromptText("New password");
        confirmPasswordField.setPromptText("Confirm new password");

        GridPane form = createFormGrid();
        addEditableRow(form, 0, "Current Password", currentPasswordField);
        addEditableRow(form, 1, "New Password", newPasswordField);
        addEditableRow(form, 2, "Confirm Password", confirmPasswordField);

        Stage stage = createModalStage("Change Password");
        Button saveButton = new Button("Change Password");
        saveButton.setDefaultButton(true);
        saveButton.setOnAction(event -> {
            String currentPassword = currentPasswordField.getText();
            String newPassword = newPasswordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            if (currentPassword == null || newPassword == null || confirmPassword == null ||
                currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Missing fields", "Please fill in all password fields.");
                return;
            }
            if (!PasswordUtil.verifyPassword(currentPassword, account.getSalt(), account.getPwdHash())) {
                showAlert(Alert.AlertType.WARNING, "Wrong password", "Current password is incorrect.");
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                showAlert(Alert.AlertType.WARNING, "Password mismatch", "New passwords do not match.");
                return;
            }
            if (newPassword.length() < 6) {
                showAlert(Alert.AlertType.WARNING, "Weak password", "Password must be at least 6 characters long.");
                return;
            }
            if (!showConfirmation("Change Password", "Do you want to change your password?")) {
                return;
            }

            String salt = PasswordUtil.generateSalt();
            account.setSalt(salt);
            account.setPwdHash(PasswordUtil.hashPassword(newPassword, salt));
            if (!userAccountDAO.update(account)) {
                showAlert(Alert.AlertType.ERROR, "Change failed", "Could not update password.");
                return;
            }

            showAlert(Alert.AlertType.INFORMATION, "Password changed", "Your password was changed.");
            stage.close();
        });

        Button closeButton = new Button("Close");
        closeButton.setCancelButton(true);
        closeButton.setOnAction(event -> stage.close());
        HBox actions = new HBox(10, saveButton, closeButton);
        actions.setStyle("-fx-alignment: center-right;");

        VBox content = new VBox(12, form, actions);
        content.setPadding(new Insets(18));
        stage.setScene(new Scene(content, 460, 260));
        stage.showAndWait();
    }

    @FXML
    public void onDeleteAccountMenuAction() {
        UserInfo teacherInfo = getCurrentTeacherInfo();
        if (teacherInfo == null || teacherInfo.getUserAccount() == null) {
            showAlert(Alert.AlertType.ERROR, "Delete unavailable", "Could not load teacher account.");
            return;
        }

        boolean confirmed = showConfirmation(
            "Delete Account",
            "Do you want to delete this account? This may remove exams, questions, attempts, and scores connected to this teacher."
        );
        if (!confirmed) {
            return;
        }

        int accountId = teacherInfo.getUserAccount().getUcId();
        if (!userAccountDAO.deleteAccountWithUserInfo(accountId)) {
            showAlert(Alert.AlertType.ERROR, "Delete failed", "Could not delete the account.");
            return;
        }

        showAlert(Alert.AlertType.INFORMATION, "Account deleted", "Your account was deleted.");
        redirectToLogin();
    }

    @FXML
    public void onLogoutMenuAction() {
        redirectToLogin();
    }

    @FXML
    public void onManageExamsButtonAction() {
        try {
            FXMLLoader loader = new FXMLLoader(ExamApplication.class.getResource("manage_exams_popup.fxml"));
            Scene scene = new Scene(loader.load(), 600, 400);
            ManageExamsController controller = loader.getController();
            controller.setCurrentTeacherId(currentTeacherId);

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Manage Exams");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onManageQuestionsButtonAction() {
        Stage stage = new Stage();
        stage.setTitle("Choose Scheduled Exam");
        stage.initModality(Modality.APPLICATION_MODAL);
        if (welcomeLabel.getScene() != null) {
            stage.initOwner(welcomeLabel.getScene().getWindow());
        }

        TableView<Exam> examTable = new TableView<>();
        examTable.setPlaceholder(new Label("No scheduled exams found."));

        TableColumn<Exam, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("examId"));
        idColumn.setPrefWidth(60);

        TableColumn<Exam, String> titleColumn = new TableColumn<>("Exam");
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("examTitle"));
        titleColumn.setPrefWidth(190);

        TableColumn<Exam, LocalDate> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("examDate"));
        dateColumn.setPrefWidth(100);

        TableColumn<Exam, Integer> durationColumn = new TableColumn<>("Duration");
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));
        durationColumn.setPrefWidth(90);

        TableColumn<Exam, String> subjectColumn = new TableColumn<>("Subject");
        subjectColumn.setCellValueFactory(cellData -> {
            Subject subject = cellData.getValue().getSubject();
            String subjectName = subject == null ? "" : subject.getSubjectName();
            return new SimpleStringProperty(subjectName);
        });
        subjectColumn.setPrefWidth(160);

        examTable.getColumns().addAll(idColumn, titleColumn, dateColumn, durationColumn, subjectColumn);
        examTable.setItems(FXCollections.observableArrayList(getScheduledExamsForCurrentTeacher()));
        examTable.setRowFactory(tableView -> {
            TableRow<Exam> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    showManageQuestionsWindow(row.getItem());
                }
            });
            return row;
        });

        Label titleLabel = new Label("Choose a scheduled exam");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Button closeButton = new Button("Close");
        closeButton.setCancelButton(true);
        closeButton.setOnAction(event -> stage.close());

        HBox actions = new HBox(closeButton);
        actions.setStyle("-fx-alignment: center-right;");

        VBox content = new VBox(12, titleLabel, examTable, actions);
        content.setPadding(new Insets(18));

        stage.setScene(new Scene(content, 640, 420));
        stage.showAndWait();
    }

    @FXML
    public void onManageSubjectsButtonAction() {
        try {
            FXMLLoader loader = new FXMLLoader(ExamApplication.class.getResource("manage_subjects_popup.fxml"));
            Scene scene = new Scene(loader.load(), 640, 400);

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Manage Subjects");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<Exam> getScheduledExamsForCurrentTeacher() {
        List<Exam> scheduledExams = new ArrayList<>();
        try {
            for (Exam exam : examDAO.getByTeacher(currentTeacherId)) {
                if ("Scheduled".equals(exam.getStatus())) {
                    scheduledExams.add(exam);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading scheduled exams: " + e.getMessage());
            e.printStackTrace();
        }
        return scheduledExams;
    }

    private void showManageQuestionsWindow(Exam exam) {
        try {
            FXMLLoader loader = new FXMLLoader(ExamApplication.class.getResource("manage_questions_popup.fxml"));
            Scene scene = new Scene(loader.load(), 700, 460);
            ManageQuestionsController controller = loader.getController();
            controller.setExam(exam);

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Manage Questions - " + exam.getExamTitle());
            stage.initModality(Modality.APPLICATION_MODAL);
            if (welcomeLabel.getScene() != null) {
                stage.initOwner(welcomeLabel.getScene().getWindow());
            }
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private UserInfo getCurrentTeacherInfo() {
        try {
            return userInfoDAO.getById(currentTeacherId);
        } catch (Exception e) {
            System.err.println("Error loading teacher info: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private GridPane createFormGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        return grid;
    }

    private void addReadOnlyRow(GridPane grid, int row, String label, String value) {
        Label labelNode = new Label(label + ":");
        labelNode.setStyle("-fx-font-weight: bold;");
        Label valueNode = new Label(value == null || value.isBlank() ? "N/A" : value);
        valueNode.setWrapText(true);
        grid.add(labelNode, 0, row);
        grid.add(valueNode, 1, row);
    }

    private void addEditableRow(GridPane grid, int row, String label, Node input) {
        Label labelNode = new Label(label + ":");
        labelNode.setStyle("-fx-font-weight: bold;");
        grid.add(labelNode, 0, row);
        grid.add(input, 1, row);
    }

    private Stage createModalStage(String title) {
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.initModality(Modality.APPLICATION_MODAL);
        if (welcomeLabel.getScene() != null) {
            stage.initOwner(welcomeLabel.getScene().getWindow());
        }
        return stage;
    }

    private void showContentWindow(String title, Node contentNode, int width, int height) {
        Stage stage = createModalStage(title);
        Button closeButton = new Button("Close");
        closeButton.setCancelButton(true);
        closeButton.setOnAction(event -> stage.close());

        HBox actions = new HBox(closeButton);
        actions.setStyle("-fx-alignment: center-right;");
        VBox content = new VBox(14, contentNode, actions);
        content.setPadding(new Insets(18));
        stage.setScene(new Scene(content, width, height));
        stage.showAndWait();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        if (welcomeLabel.getScene() != null) {
            alert.initOwner(welcomeLabel.getScene().getWindow());
        }
        alert.showAndWait();
    }

    private boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.CANCEL, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        if (welcomeLabel.getScene() != null) {
            alert.initOwner(welcomeLabel.getScene().getWindow());
        }
        return alert.showAndWait().filter(buttonType -> buttonType == ButtonType.OK).isPresent();
    }

    private void redirectToLogin() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(ExamApplication.class.getResource("login_screen.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 400, 500);
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setTitle("Login");
            stage.setScene(scene);
            stage.setMaximized(false);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Logout failed", "Could not load login screen.");
        }
    }

    /**
     * Inner class to represent a row in the attempts table
     */
    public static class ExamAttemptRow {
        private final String studentName;
        private final String examName;
        private final String startTime;
        private final String endTime;
        private final int score;
        private final String status;
        private final String duration;

        public ExamAttemptRow(String studentName, String examName, String startTime, String endTime, int score, String status, String duration) {
            this.studentName = studentName;
            this.examName = examName;
            this.startTime = startTime;
            this.endTime = endTime;
            this.score = score;
            this.status = status;
            this.duration = duration;
        }

        public String getStudentName() { return studentName; }
        public String getExamName() { return examName; }
        public String getStartTime() { return startTime; }
        public String getEndTime() { return endTime; }
        public int getScore() { return score; }
        public String getStatus() { return status; }
        public String getDuration() { return duration; }
    }
}
