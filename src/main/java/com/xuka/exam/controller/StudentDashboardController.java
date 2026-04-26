package com.xuka.exam.controller;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import com.xuka.exam.ExamApplication;
import com.xuka.exam.dao.ExamAttemptDAO;
import com.xuka.exam.dao.ExamDAO;
import com.xuka.exam.dao.ExamQuestionDAO;
import com.xuka.exam.dao.SubjectDAO;
import com.xuka.exam.dao.SubjectRegistrationDAO;
import com.xuka.exam.dao.UserAccountDAO;
import com.xuka.exam.dao.UserInfoDAO;
import com.xuka.exam.models.Exam;
import com.xuka.exam.models.ExamAttempt;
import com.xuka.exam.models.ExamQuestion;
import com.xuka.exam.models.Question;
import com.xuka.exam.models.Subject;
import com.xuka.exam.models.SubjectRegistration;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controller for the student dashboard.
 * Shows enrolled subjects, available exams, and recent attempt results.
 */
public class StudentDashboardController implements Initializable {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label dateTimeLabel;

    @FXML
    private Label enrolledSubjectsLabel;

    @FXML
    private Label availableExamsLabel;

    @FXML
    private Label completedAttemptsLabel;

    @FXML
    private Label averageScoreLabel;

    @FXML
    private Label selectedSubjectLabel;

    @FXML
    private TableView<AvailableExamRow> availableExamsTable;

    @FXML
    private TableColumn<AvailableExamRow, String> examTitleColumn;

    @FXML
    private TableColumn<AvailableExamRow, String> subjectColumn;

    @FXML
    private TableColumn<AvailableExamRow, String> examDateColumn;

    @FXML
    private TableColumn<AvailableExamRow, Integer> durationColumn;

    @FXML
    private TableColumn<AvailableExamRow, Integer> totalMarksColumn;

    @FXML
    private TableColumn<AvailableExamRow, String> examStatusColumn;

    @FXML
    private TableView<AttemptRow> attemptsTable;

    @FXML
    private TableColumn<AttemptRow, String> attemptExamColumn;

    @FXML
    private TableColumn<AttemptRow, String> attemptSubjectColumn;

    @FXML
    private TableColumn<AttemptRow, String> startTimeColumn;

    @FXML
    private TableColumn<AttemptRow, String> endTimeColumn;

    @FXML
    private TableColumn<AttemptRow, Integer> scoreColumn;

    @FXML
    private TableColumn<AttemptRow, String> attemptStatusColumn;

    private SubjectRegistrationDAO subjectRegistrationDAO;
    private SubjectDAO subjectDAO;
    private ExamDAO examDAO;
    private ExamQuestionDAO examQuestionDAO;
    private ExamAttemptDAO examAttemptDAO;
    private UserInfoDAO userInfoDAO;
    private UserAccountDAO userAccountDAO;
    private int currentStudentId;
    private Integer selectedSubjectId;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        subjectRegistrationDAO = new SubjectRegistrationDAO();
        subjectDAO = new SubjectDAO();
        examDAO = new ExamDAO();
        examQuestionDAO = new ExamQuestionDAO();
        examAttemptDAO = new ExamAttemptDAO();
        userInfoDAO = new UserInfoDAO();
        userAccountDAO = new UserAccountDAO();

        initializeTableColumns();
        updateDateTime();
    }

    private void initializeTableColumns() {
        examTitleColumn.setCellValueFactory(new PropertyValueFactory<>("examTitle"));
        subjectColumn.setCellValueFactory(new PropertyValueFactory<>("subjectName"));
        examDateColumn.setCellValueFactory(new PropertyValueFactory<>("examDate"));
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));
        totalMarksColumn.setCellValueFactory(new PropertyValueFactory<>("totalMarks"));
        examStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        attemptExamColumn.setCellValueFactory(new PropertyValueFactory<>("examTitle"));
        attemptSubjectColumn.setCellValueFactory(new PropertyValueFactory<>("subjectName"));
        startTimeColumn.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        endTimeColumn.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        scoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));
        attemptStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        subjectColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSubjectName()));
        attemptSubjectColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSubjectName()));

        examStatusColumn.setCellFactory(column -> statusCell());
        attemptStatusColumn.setCellFactory(column -> statusCell());
        scoreColumn.setCellFactory(column -> scoreCell());

        availableExamsTable.setRowFactory(table -> {
            TableRow<AvailableExamRow> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    showTakeExamWindow(row.getItem());
                }
            });
            return row;
        });
    }

    private <T> TableCell<T, String> statusCell() {
        return new TableCell<T, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                setText(item);
                if ("Completed".equals(item)) {
                    setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
                } else if ("Ongoing".equals(item) || "In Progress".equals(item)) {
                    setStyle("-fx-text-fill: #ef6c00; -fx-font-weight: bold;");
                } else {
                    setStyle("-fx-text-fill: #1565c0; -fx-font-weight: bold;");
                }
            }
        };
    }

    private TableCell<AttemptRow, Integer> scoreCell() {
        return new TableCell<AttemptRow, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                setText(item.toString());
                if (item >= 80) {
                    setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
                } else if (item >= 60) {
                    setStyle("-fx-text-fill: #ef6c00; -fx-font-weight: bold;");
                } else {
                    setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");
                }
            }
        };
    }

    public void setCurrentStudentId(int studentId) {
        this.currentStudentId = studentId;
        loadDashboardData();
    }

    public void setWelcomeMessage(String studentName) {
        welcomeLabel.setText("Welcome, " + studentName + " - Student Dashboard");
    }

    @FXML
    private void onRefreshButtonAction() {
        loadDashboardData();
    }

    @FXML
    private void onRegisterSubjectButtonAction() {
        Stage stage = createSubjectSelectionStage("Register Subject", "Choose a subject to register", SubjectAction.REGISTER);
        stage.showAndWait();
    }

    @FXML
    private void onSelectSubjectButtonAction() {
        Stage stage = createSubjectSelectionStage("Select Subject", "Choose an enrolled subject for exams", SubjectAction.SELECT);
        stage.showAndWait();
    }

    @FXML
    private void onUnregisterSubjectButtonAction() {
        Stage stage = createSubjectSelectionStage("Undo Registration", "Choose a registered subject to remove", SubjectAction.UNREGISTER);
        stage.showAndWait();
    }

    @FXML
    private void onShowAllSubjectsButtonAction() {
        selectedSubjectId = null;
        selectedSubjectLabel.setText("Showing exams for all enrolled subjects");
        loadDashboardData();
    }

    @FXML
    private void onTakeExamButtonAction() {
        AvailableExamRow selectedExam = availableExamsTable.getSelectionModel().getSelectedItem();
        if (selectedExam == null) {
            showAlert(Alert.AlertType.WARNING, "No exam selected", "Please choose an exam from the available exams table.");
            return;
        }

        showTakeExamWindow(selectedExam);
    }

    @FXML
    private void onProfileMenuAction() {
        UserInfo studentInfo = getCurrentStudentInfo();
        if (studentInfo == null) {
            showAlert(Alert.AlertType.ERROR, "Profile unavailable", "Could not load student profile.");
            return;
        }

        UserAccount account = studentInfo.getUserAccount();
        GridPane profileGrid = createFormGrid();
        addReadOnlyRow(profileGrid, 0, "Username", account == null ? "N/A" : account.getUsername());
        addReadOnlyRow(profileGrid, 1, "Full Name", studentInfo.getFullName());
        addReadOnlyRow(profileGrid, 2, "Email", studentInfo.getEmail());
        addReadOnlyRow(profileGrid, 3, "Phone", studentInfo.getPhone());
        addReadOnlyRow(profileGrid, 4, "Date of Birth", studentInfo.getDateOfBirth() == null ? "N/A" : studentInfo.getDateOfBirth().toString());
        addReadOnlyRow(profileGrid, 5, "Department", studentInfo.getDepartment());
        addReadOnlyRow(profileGrid, 6, "Role", "Student");

        showContentWindow("Profile", profileGrid, 420, 330);
    }

    @FXML
    private void onEditProfileMenuAction() {
        UserInfo studentInfo = getCurrentStudentInfo();
        if (studentInfo == null || studentInfo.getUserAccount() == null) {
            showAlert(Alert.AlertType.ERROR, "Edit unavailable", "Could not load student account.");
            return;
        }

        UserAccount account = studentInfo.getUserAccount();
        TextField usernameField = new TextField(account.getUsername());
        TextField fullNameField = new TextField(studentInfo.getFullName());
        TextField emailField = new TextField(studentInfo.getEmail());
        TextField phoneField = new TextField(studentInfo.getPhone() == null ? "" : studentInfo.getPhone());
        TextField departmentField = new TextField(studentInfo.getDepartment() == null ? "" : studentInfo.getDepartment());
        DatePicker dobPicker = new DatePicker(studentInfo.getDateOfBirth());

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
            studentInfo.setFullName(fullName);
            studentInfo.setEmail(email);
            studentInfo.setPhone(phoneField.getText());
            studentInfo.setDateOfBirth(dobPicker.getValue());
            studentInfo.setDepartment(departmentField.getText());

            boolean accountUpdated = userAccountDAO.update(account);
            boolean infoUpdated = userInfoDAO.update(studentInfo);
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
    private void onChangePasswordMenuAction() {
        UserInfo studentInfo = getCurrentStudentInfo();
        if (studentInfo == null || studentInfo.getUserAccount() == null) {
            showAlert(Alert.AlertType.ERROR, "Password unavailable", "Could not load student account.");
            return;
        }

        UserAccount account = studentInfo.getUserAccount();
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
    private void onDeleteAccountMenuAction() {
        UserInfo studentInfo = getCurrentStudentInfo();
        if (studentInfo == null || studentInfo.getUserAccount() == null) {
            showAlert(Alert.AlertType.ERROR, "Delete unavailable", "Could not load student account.");
            return;
        }

        boolean confirmed = showConfirmation(
            "Delete Account",
            "Do you want to delete this account? This may remove subject registrations, attempts, and scores connected to this student."
        );
        if (!confirmed) {
            return;
        }

        int accountId = studentInfo.getUserAccount().getUcId();
        if (!userAccountDAO.deleteAccountWithUserInfo(accountId)) {
            showAlert(Alert.AlertType.ERROR, "Delete failed", "Could not delete the account.");
            return;
        }

        showAlert(Alert.AlertType.INFORMATION, "Account deleted", "Your account was deleted.");
        redirectToLogin();
    }

    @FXML
    private void onLogoutMenuAction() {
        redirectToLogin();
    }

    private void loadDashboardData() {
        new Thread(() -> {
            try {
                List<SubjectRegistration> registrations = subjectRegistrationDAO.getByUserInfo(currentStudentId);
                List<Exam> availableExams = getAvailableExams(registrations);
                List<ExamAttempt> attempts = examAttemptDAO.getByStudent(currentStudentId);
                List<ExamAttempt> visibleAttempts = getVisibleAttempts(attempts, registrations);

                Platform.runLater(() -> {
                    updateSummaryCards(registrations, availableExams, visibleAttempts);
                    updateAvailableExamsTable(availableExams, attempts);
                    updateAttemptsTable(visibleAttempts);
                });
            } catch (Exception e) {
                System.err.println("Error loading student dashboard data: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private List<Exam> getAvailableExams(List<SubjectRegistration> registrations) {
        List<Exam> exams = new ArrayList<>();
        Set<Integer> seenExamIds = new HashSet<>();

        for (SubjectRegistration registration : registrations) {
            Subject subject = registration.getSubject();
            if (subject == null) {
                continue;
            }
            if (selectedSubjectId != null && subject.getSubjectId() != selectedSubjectId) {
                continue;
            }

            for (Exam exam : examDAO.getBySubject(subject.getSubjectId())) {
                if (seenExamIds.add(exam.getExamId())) {
                    exams.add(exam);
                }
            }
        }

        exams.sort(Comparator.comparing(Exam::getExamDate, Comparator.nullsLast(Comparator.naturalOrder())));
        return exams;
    }

    private List<ExamAttempt> getVisibleAttempts(List<ExamAttempt> attempts, List<SubjectRegistration> registrations) {
        Set<Integer> registeredSubjectIds = new HashSet<>();
        for (SubjectRegistration registration : registrations) {
            Subject subject = registration.getSubject();
            if (subject != null) {
                registeredSubjectIds.add(subject.getSubjectId());
            }
        }

        List<ExamAttempt> visibleAttempts = new ArrayList<>();
        for (ExamAttempt attempt : attempts) {
            Exam exam = attempt.getExam();
            Subject subject = exam == null ? null : exam.getSubject();
            if (subject == null || !registeredSubjectIds.contains(subject.getSubjectId())) {
                continue;
            }
            if (selectedSubjectId != null && subject.getSubjectId() != selectedSubjectId) {
                continue;
            }
            visibleAttempts.add(attempt);
        }

        return visibleAttempts;
    }

    private void updateSummaryCards(List<SubjectRegistration> registrations, List<Exam> availableExams, List<ExamAttempt> attempts) {
        long completedAttempts = attempts.stream()
            .filter(attempt -> "Completed".equals(attempt.getStatus()))
            .count();
        double averageScore = attempts.stream()
            .filter(attempt -> "Completed".equals(attempt.getStatus()))
            .mapToInt(ExamAttempt::getScore)
            .average()
            .orElse(0.0);

        enrolledSubjectsLabel.setText(String.valueOf(registrations.size()));
        availableExamsLabel.setText(String.valueOf(availableExams.size()));
        completedAttemptsLabel.setText(String.valueOf(completedAttempts));
        averageScoreLabel.setText(String.format("%.1f", averageScore));
    }

    private void updateAvailableExamsTable(List<Exam> exams, List<ExamAttempt> attempts) {
        Set<Integer> attemptedExamIds = new HashSet<>();
        for (ExamAttempt attempt : attempts) {
            if (attempt.getExam() != null) {
                attemptedExamIds.add(attempt.getExam().getExamId());
            }
        }

        ObservableList<AvailableExamRow> rows = FXCollections.observableArrayList();
        for (Exam exam : exams) {
            String status = attemptedExamIds.contains(exam.getExamId()) ? "Attempted" : safeText(exam.getStatus(), "Scheduled");
            rows.add(new AvailableExamRow(
                exam.getExamId(),
                exam.getExamTitle(),
                getSubjectName(exam),
                formatDate(exam.getExamDate()),
                exam.getDuration(),
                exam.getTotalMarks(),
                status
            ));
        }

        availableExamsTable.setItems(rows);
    }

    private void updateAttemptsTable(List<ExamAttempt> attempts) {
        ObservableList<AttemptRow> rows = FXCollections.observableArrayList();
        for (ExamAttempt attempt : attempts) {
            Exam exam = attempt.getExam();
            rows.add(new AttemptRow(
                exam == null ? "N/A" : exam.getExamTitle(),
                exam == null ? "N/A" : getSubjectName(exam),
                formatDateTime(attempt.getStartTime()),
                attempt.getEndTime() == null ? "N/A" : formatDateTime(attempt.getEndTime()),
                attempt.getScore(),
                safeText(attempt.getStatus(), "N/A")
            ));
        }

        attemptsTable.setItems(rows);
    }

    private void showTakeExamWindow(AvailableExamRow examRow) {
        if ("Attempted".equals(examRow.getStatus())) {
            boolean confirmed = showConfirmation(
                "Retake Exam",
                "You already attempted this exam. Taking it again will create another attempt record."
            );
            if (!confirmed) {
                return;
            }
        }

        List<ExamQuestion> examQuestions = examQuestionDAO.getByExam(examRow.getExamId());
        if (examQuestions.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No questions", "This exam does not have any questions yet.");
            return;
        }

        Stage stage = new Stage();
        stage.setTitle("Take Exam - " + examRow.getExamTitle());
        stage.initModality(Modality.APPLICATION_MODAL);
        if (welcomeLabel.getScene() != null) {
            stage.initOwner(welcomeLabel.getScene().getWindow());
        }

        Label titleLabel = new Label(examRow.getExamTitle());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label subjectLabel = new Label(examRow.getSubjectName() + " | " + examRow.getDuration() + " min | " + examRow.getTotalMarks() + " marks");
        subjectLabel.setStyle("-fx-text-fill: #455a64;");

        VBox questionsBox = new VBox(14);
        Map<Integer, TextArea> answerFields = new LinkedHashMap<>();
        int questionNumber = 1;
        for (ExamQuestion examQuestion : examQuestions) {
            Question question = examQuestion.getQuestion();
            if (question == null) {
                continue;
            }

            Label questionLabel = new Label(questionNumber + ". " + question.getQuestionText() + " (" + question.getMarks() + " marks)");
            questionLabel.setWrapText(true);
            questionLabel.setStyle("-fx-font-weight: bold;");

            TextArea answerArea = new TextArea();
            answerArea.setPromptText("Enter your answer");
            answerArea.setWrapText(true);
            answerArea.setPrefRowCount(3);
            answerFields.put(question.getQuestionId(), answerArea);

            VBox questionPane = new VBox(6, questionLabel, answerArea);
            questionPane.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-background-color: #fafafa; -fx-padding: 12;");
            questionsBox.getChildren().add(questionPane);
            questionNumber++;
        }

        ScrollPane questionsScrollPane = new ScrollPane(questionsBox);
        questionsScrollPane.setFitToWidth(true);
        questionsScrollPane.setPrefHeight(520);

        Button submitButton = new Button("Submit Exam");
        submitButton.setDefaultButton(true);
        submitButton.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        submitButton.setOnAction(event -> submitExam(examRow, answerFields, stage));

        Button closeButton = new Button("Close");
        closeButton.setCancelButton(true);
        closeButton.setOnAction(event -> stage.close());

        HBox actions = new HBox(10, submitButton, closeButton);
        actions.setStyle("-fx-alignment: center-right;");

        VBox content = new VBox(12, titleLabel, subjectLabel, questionsScrollPane, actions);
        content.setPadding(new Insets(18));
        stage.setScene(new Scene(content, 760, 700));
        stage.showAndWait();
    }

    private void submitExam(AvailableExamRow examRow, Map<Integer, TextArea> answerFields, Stage stage) {
        boolean confirmed = showConfirmation("Submit Exam", "Submit this exam now?");
        if (!confirmed) {
            return;
        }

        Map<Integer, String> answersByQuestionId = new LinkedHashMap<>();
        for (Map.Entry<Integer, TextArea> entry : answerFields.entrySet()) {
            answersByQuestionId.put(entry.getKey(), entry.getValue().getText());
        }

        boolean submitted = examAttemptDAO.submitCompletedAttempt(currentStudentId, examRow.getExamId(), answersByQuestionId);
        if (!submitted) {
            showAlert(Alert.AlertType.ERROR, "Submit failed", "Could not save your exam attempt.");
            return;
        }

        loadDashboardData();
        showAlert(Alert.AlertType.INFORMATION, "Exam submitted", "Your exam attempt has been saved.");
        stage.close();
    }

    private Stage createSubjectSelectionStage(String title, String heading, SubjectAction action) {
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.initModality(Modality.APPLICATION_MODAL);
        if (welcomeLabel.getScene() != null) {
            stage.initOwner(welcomeLabel.getScene().getWindow());
        }

        TableView<SubjectRow> subjectTable = new TableView<>();
        boolean enrolledOnly = action != SubjectAction.REGISTER;
        subjectTable.setPlaceholder(new Label(enrolledOnly ? "No registered subjects found." : "No subjects found."));

        TableColumn<SubjectRow, String> codeColumn = new TableColumn<>("Code");
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("subjectCode"));
        codeColumn.setPrefWidth(120);

        TableColumn<SubjectRow, String> nameColumn = new TableColumn<>("Subject");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("subjectName"));
        nameColumn.setPrefWidth(220);

        TableColumn<SubjectRow, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        descriptionColumn.setPrefWidth(260);

        subjectTable.getColumns().addAll(codeColumn, nameColumn, descriptionColumn);
        subjectTable.setItems(FXCollections.observableArrayList(loadSubjectRows(enrolledOnly)));
        subjectTable.setRowFactory(table -> {
            TableRow<SubjectRow> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    handleSubjectChoice(row.getItem(), action, stage);
                }
            });
            return row;
        });

        Label titleLabel = new Label(heading);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Button chooseButton = new Button(getSubjectActionText(action));
        chooseButton.setDefaultButton(true);
        chooseButton.setOnAction(event -> {
            SubjectRow selectedSubject = subjectTable.getSelectionModel().getSelectedItem();
            if (selectedSubject == null) {
                showAlert(Alert.AlertType.WARNING, "No subject selected", "Please choose a subject first.");
                return;
            }
            handleSubjectChoice(selectedSubject, action, stage);
        });

        Button closeButton = new Button("Close");
        closeButton.setCancelButton(true);
        closeButton.setOnAction(event -> stage.close());

        HBox actions = new HBox(10, chooseButton, closeButton);
        actions.setStyle("-fx-alignment: center-right;");

        VBox content = new VBox(12, titleLabel, subjectTable, actions);
        content.setPadding(new Insets(18));
        stage.setScene(new Scene(content, 660, 430));
        return stage;
    }

    private List<SubjectRow> loadSubjectRows(boolean enrolledOnly) {
        Set<Integer> registeredSubjectIds = new HashSet<>();
        List<SubjectRow> rows = new ArrayList<>();

        for (SubjectRegistration registration : subjectRegistrationDAO.getByUserInfo(currentStudentId)) {
            Subject subject = registration.getSubject();
            if (subject != null) {
                registeredSubjectIds.add(subject.getSubjectId());
                if (enrolledOnly) {
                    rows.add(new SubjectRow(subject));
                }
            }
        }

        if (!enrolledOnly) {
            for (Subject subject : subjectDAO.getAll()) {
                if (!registeredSubjectIds.contains(subject.getSubjectId())) {
                    rows.add(new SubjectRow(subject));
                }
            }
        }

        rows.sort(Comparator.comparing(SubjectRow::getSubjectCode, Comparator.nullsLast(String::compareToIgnoreCase)));
        return rows;
    }

    private void handleSubjectChoice(SubjectRow subject, SubjectAction action, Stage stage) {
        if (action == SubjectAction.SELECT) {
            selectedSubjectId = subject.getSubjectId();
            selectedSubjectLabel.setText("Showing exams for " + subject.getSubjectCode() + " - " + subject.getSubjectName());
            loadDashboardData();
            stage.close();
            return;
        }

        if (action == SubjectAction.UNREGISTER) {
            boolean confirmed = showConfirmation(
                "Undo Registration",
                "WARNING: removing subject may lost all attempts and scores record"
            );
            if (!confirmed) {
                return;
            }

            boolean deleted = subjectRegistrationDAO.delete(subject.getSubjectId(), currentStudentId);
            if (!deleted) {
                showAlert(Alert.AlertType.ERROR, "Undo registration failed", "Could not remove this subject registration.");
                return;
            }

            if (selectedSubjectId != null && selectedSubjectId == subject.getSubjectId()) {
                selectedSubjectId = null;
                selectedSubjectLabel.setText("Showing exams for all enrolled subjects");
            }

            loadDashboardData();
            showAlert(Alert.AlertType.INFORMATION, "Registration removed", "Subject registration removed.");
            stage.close();
            return;
        }

        boolean registered = subjectRegistrationDAO.registerByIds(subject.getSubjectId(), currentStudentId);
        if (!registered) {
            showAlert(Alert.AlertType.ERROR, "Registration failed", "Could not register this subject.");
            return;
        }

        selectedSubjectId = subject.getSubjectId();
        selectedSubjectLabel.setText("Showing exams for " + subject.getSubjectCode() + " - " + subject.getSubjectName());
        loadDashboardData();
        showAlert(Alert.AlertType.INFORMATION, "Subject registered", "Subject registration saved.");
        stage.close();
    }

    private String getSubjectActionText(SubjectAction action) {
        if (action == SubjectAction.REGISTER) {
            return "Register Subject";
        }
        if (action == SubjectAction.UNREGISTER) {
            return "Undo Registration";
        }
        return "Select Subject";
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        if (welcomeLabel.getScene() != null) {
            alert.initOwner(welcomeLabel.getScene().getWindow());
        }
        alert.getDialogPane().lookupButton(ButtonType.OK).setStyle("-fx-font-weight: bold;");
        alert.showAndWait();
    }

    private boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.CANCEL, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        if (welcomeLabel.getScene() != null) {
            alert.initOwner(welcomeLabel.getScene().getWindow());
        }
        alert.getDialogPane().lookupButton(ButtonType.OK).setStyle("-fx-font-weight: bold; -fx-text-fill: #c62828;");
        return alert.showAndWait().filter(buttonType -> buttonType == ButtonType.OK).isPresent();
    }

    private UserInfo getCurrentStudentInfo() {
        try {
            return userInfoDAO.getById(currentStudentId);
        } catch (Exception e) {
            System.err.println("Error loading student info: " + e.getMessage());
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

    private String getSubjectName(Exam exam) {
        return exam.getSubject() == null ? "N/A" : exam.getSubject().getSubjectName();
    }

    private String safeText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String formatDate(LocalDate date) {
        if (date == null) {
            return "N/A";
        }
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "N/A";
        }
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    private void updateDateTime() {
        Thread clockThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                Platform.runLater(() -> {
                    LocalDateTime now = LocalDateTime.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy 'at' HH:mm:ss");
                    dateTimeLabel.setText(now.format(formatter));
                });

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        clockThread.setDaemon(true);
        clockThread.start();
    }

    public static class AvailableExamRow {
        private final int examId;
        private final String examTitle;
        private final String subjectName;
        private final String examDate;
        private final int duration;
        private final int totalMarks;
        private final String status;

        public AvailableExamRow(int examId, String examTitle, String subjectName, String examDate, int duration, int totalMarks, String status) {
            this.examId = examId;
            this.examTitle = examTitle;
            this.subjectName = subjectName;
            this.examDate = examDate;
            this.duration = duration;
            this.totalMarks = totalMarks;
            this.status = status;
        }

        public int getExamId() { return examId; }
        public String getExamTitle() { return examTitle; }
        public String getSubjectName() { return subjectName; }
        public String getExamDate() { return examDate; }
        public int getDuration() { return duration; }
        public int getTotalMarks() { return totalMarks; }
        public String getStatus() { return status; }
    }

    public static class AttemptRow {
        private final String examTitle;
        private final String subjectName;
        private final String startTime;
        private final String endTime;
        private final int score;
        private final String status;

        public AttemptRow(String examTitle, String subjectName, String startTime, String endTime, int score, String status) {
            this.examTitle = examTitle;
            this.subjectName = subjectName;
            this.startTime = startTime;
            this.endTime = endTime;
            this.score = score;
            this.status = status;
        }

        public String getExamTitle() { return examTitle; }
        public String getSubjectName() { return subjectName; }
        public String getStartTime() { return startTime; }
        public String getEndTime() { return endTime; }
        public int getScore() { return score; }
        public String getStatus() { return status; }
    }

    public static class SubjectRow {
        private final int subjectId;
        private final String subjectCode;
        private final String subjectName;
        private final String description;

        public SubjectRow(Subject subject) {
            this.subjectId = subject.getSubjectId();
            this.subjectCode = subject.getSubjectCode();
            this.subjectName = subject.getSubjectName();
            this.description = subject.getDescription() == null ? "" : subject.getDescription();
        }

        public int getSubjectId() { return subjectId; }
        public String getSubjectCode() { return subjectCode; }
        public String getSubjectName() { return subjectName; }
        public String getDescription() { return description; }
    }

    private enum SubjectAction {
        REGISTER,
        SELECT,
        UNREGISTER
    }
}
