package com.xuka.exam.controller;

import java.time.LocalDate;
import java.util.List;

import com.xuka.exam.dao.ExamDAO;
import com.xuka.exam.dao.SubjectDAO;
import com.xuka.exam.dao.UserInfoDAO;
import com.xuka.exam.models.Exam;
import com.xuka.exam.models.Subject;
import com.xuka.exam.models.UserInfo;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class ManageExamsController {

    @FXML
    private TableView<Exam> examTable;

    @FXML
    private TableColumn<Exam, Integer> examIdColumn;

    @FXML
    private TableColumn<Exam, String> examNameColumn;

    @FXML
    private TableColumn<Exam, LocalDate> examDateColumn;

    @FXML
    private TableColumn<Exam, Integer> examDurationColumn;

    @FXML
    private TableColumn<Exam, String> examSubjectColumn;

    @FXML
    private TableColumn<Exam, Void> examActionColumn;

    private final ExamDAO examDAO = new ExamDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final UserInfoDAO userInfoDAO = new UserInfoDAO();
    private int currentTeacherId = 1;

    @FXML
    private void initialize() {
        examIdColumn.setCellValueFactory(new PropertyValueFactory<>("examId"));
        examNameColumn.setCellValueFactory(new PropertyValueFactory<>("examTitle"));
        examDateColumn.setCellValueFactory(new PropertyValueFactory<>("examDate"));
        examDurationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));
        examSubjectColumn.setCellValueFactory(cellData -> {
            Subject subject = cellData.getValue().getSubject();
            String subjectName = subject != null ? subject.getSubjectName() : "";
            return new SimpleStringProperty(subjectName);
        });
        setupActionColumn();
        loadExams();
    }

    public void setCurrentTeacherId(int currentTeacherId) {
        this.currentTeacherId = currentTeacherId;
        loadExams();
    }

    private void loadExams() {
        try {
            List<Exam> exams = examDAO.getByTeacher(currentTeacherId);
            examTable.setItems(FXCollections.observableArrayList(exams));
        } catch (Exception e) {
            System.err.println("Error loading exams from database: " + e.getMessage());
            loadMockExams();
        }
    }

    private void loadMockExams() {
        Subject database = new Subject("Database Management", "DBM101");
        Subject programming = new Subject("Java Programming", "JAVA201");
        Subject networking = new Subject("Computer Networks", "NET301");

        Exam midterm = createMockExam(101, "Database Midterm", LocalDate.now().plusDays(3), 90, database);
        Exam finalExam = createMockExam(102, "Java Final Practical", LocalDate.now().plusWeeks(2), 120, programming);
        Exam quiz = createMockExam(103, "Networking Quiz 1", LocalDate.now().plusDays(7), 45, networking);
        Exam review = createMockExam(104, "SQL Review Test", LocalDate.now().plusDays(10), 60, database);

        examTable.setItems(FXCollections.observableArrayList(midterm, finalExam, quiz, review));
    }

    private Exam createMockExam(int id, String title, LocalDate date, int duration, Subject subject) {
        Exam exam = new Exam();
        exam.setExamId(id);
        exam.setExamTitle(title);
        exam.setExamDate(date);
        exam.setDuration(duration);
        exam.setTotalMarks(100);
        exam.setStatus("Scheduled");
        exam.setSubject(subject);
        return exam;
    }

    private void setupActionColumn() {
        examActionColumn.setCellFactory(column -> new TableCell<>() {
            private final Button editButton = createIconButton(
                    "M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25z M20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34a.9959.9959 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z",
                    "Edit exam",
                    "#1976d2");
            private final Button deleteButton = createIconButton(
                    "M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12z M8 9h8v10H8V9z M15.5 4l-1-1h-5l-1 1H5v2h14V4z",
                    "Delete exam",
                    "#d32f2f");
            private final HBox actionBox = new HBox(6, editButton, deleteButton);

            {
                actionBox.setStyle("-fx-alignment: center;");

                editButton.setOnAction(event -> {
                    Exam exam = getExamForCurrentRow();
                    if (exam != null) {
                        examTable.getSelectionModel().select(exam);
                        editExam(exam);
                    }
                });

                deleteButton.setOnAction(event -> {
                    Exam exam = getExamForCurrentRow();
                    if (exam != null) {
                        examTable.getSelectionModel().select(exam);
                        deleteExam(exam);
                    }
                });
            }

            private Exam getExamForCurrentRow() {
                int rowIndex = getIndex();
                if (rowIndex < 0 || rowIndex >= getTableView().getItems().size()) {
                    return null;
                }
                return getTableView().getItems().get(rowIndex);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : actionBox);
                setText(null);
            }
        });
    }

    private Button createIconButton(String svgContent, String tooltipText, String color) {
        SVGPath icon = new SVGPath();
        icon.setContent(svgContent);
        icon.setFill(Color.web(color));
        icon.setScaleX(0.72);
        icon.setScaleY(0.72);

        Button button = new Button();
        button.setGraphic(icon);
        button.setTooltip(new Tooltip(tooltipText));
        button.setMinSize(28, 28);
        button.setPrefSize(28, 28);
        button.setMaxSize(28, 28);
        button.setStyle("-fx-background-color: transparent; -fx-padding: 4; -fx-cursor: hand;");
        return button;
    }

    @FXML
    private void onEditExamButtonAction() {
        editExam(examTable.getSelectionModel().getSelectedItem());
    }

    @FXML
    private void onDeleteExamButtonAction() {
        deleteExam(examTable.getSelectionModel().getSelectedItem());
    }

    @FXML
    private void onAddExamButtonAction() {
        showExamPopup(null);
    }

    private void editExam(Exam exam) {
        if (exam != null) {
            showExamPopup(exam);
        }
    }

    private void deleteExam(Exam exam) {
        if (exam == null) {
            showAlert(Alert.AlertType.WARNING, "No exam selected", "Please select an exam first.");
            return;
        }

        Alert confirmAlert = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Delete \"" + exam.getExamTitle() + "\"?\nRelated questions and attempts may also be deleted.",
                ButtonType.CANCEL,
                ButtonType.OK
        );
        confirmAlert.setTitle("Delete Exam");
        confirmAlert.setHeaderText(null);
        if (examTable.getScene() != null) {
            confirmAlert.initOwner(examTable.getScene().getWindow());
        }

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        if (examDAO.delete(exam.getExamId())) {
            loadExams();
            showAlert(Alert.AlertType.INFORMATION, "Exam deleted", "Exam deleted successfully.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Delete failed", "Could not delete the exam. Please try again.");
        }
    }

    private void showExamPopup(Exam examToEdit) {
        Stage popup = new Stage();
        boolean editing = examToEdit != null;
        popup.setTitle(editing ? "Edit Exam" : "Add Exam");
        popup.initModality(Modality.APPLICATION_MODAL);

        if (examTable.getScene() != null) {
            popup.initOwner(examTable.getScene().getWindow());
        }

        TextField titleField = new TextField(editing ? examToEdit.getExamTitle() : "");
        titleField.setPromptText("Exam title");

        DatePicker datePicker = new DatePicker(editing ? examToEdit.getExamDate() : LocalDate.now());

        TextField durationField = new TextField(editing ? String.valueOf(examToEdit.getDuration()) : "");
        durationField.setPromptText("Minutes");

        TextField totalMarksField = new TextField(editing ? String.valueOf(examToEdit.getTotalMarks()) : "100");
        totalMarksField.setPromptText("Total marks");

        ComboBox<String> statusComboBox = new ComboBox<>(
                FXCollections.observableArrayList("Scheduled", "Ongoing", "Completed"));
        statusComboBox.setValue(editing && examToEdit.getStatus() != null ? examToEdit.getStatus() : "Scheduled");

        ComboBox<Subject> subjectComboBox = new ComboBox<>();
        subjectComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Subject subject) {
                if (subject == null) {
                    return "";
                }
                return subject.getSubjectCode() + " - " + subject.getSubjectName();
            }

            @Override
            public Subject fromString(String string) {
                return null;
            }
        });

        try {
            List<Subject> subjects = subjectDAO.getAll();
            subjectComboBox.setItems(FXCollections.observableArrayList(subjects));
            if (editing && examToEdit.getSubject() != null) {
                selectSubject(subjectComboBox, examToEdit.getSubject().getSubjectId());
            }
            if (subjectComboBox.getValue() == null && !subjects.isEmpty()) {
                subjectComboBox.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Subjects unavailable",
                    "Could not load subjects from the database. Please add a subject first.");
            popup.close();
            return;
        }

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.setPadding(new Insets(18));
        form.add(new Label("Title"), 0, 0);
        form.add(titleField, 1, 0);
        form.add(new Label("Date"), 0, 1);
        form.add(datePicker, 1, 1);
        form.add(new Label("Duration"), 0, 2);
        form.add(durationField, 1, 2);
        form.add(new Label("Total Marks"), 0, 3);
        form.add(totalMarksField, 1, 3);
        form.add(new Label("Status"), 0, 4);
        form.add(statusComboBox, 1, 4);
        form.add(new Label("Subject"), 0, 5);
        form.add(subjectComboBox, 1, 5);

        Button saveButton = new Button("Save");
        saveButton.setDefaultButton(true);
        saveButton.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-font-weight: bold;");

        Button cancelButton = new Button("Cancel");
        cancelButton.setCancelButton(true);
        cancelButton.setOnAction(event -> popup.close());

        HBox actions = new HBox(10, cancelButton, saveButton);
        actions.setStyle("-fx-alignment: center-right;");

        VBox content = new VBox(8, form, actions);
        content.setPadding(new Insets(6, 6, 14, 6));

        saveButton.setOnAction(event -> {
            Exam exam = buildExamFromForm(
                    examToEdit,
                    titleField,
                    datePicker,
                    durationField,
                    totalMarksField,
                    statusComboBox,
                    subjectComboBox
            );

            if (exam == null) {
                return;
            }

            boolean saved = editing ? examDAO.update(exam) : examDAO.save(exam);
            if (saved) {
                loadExams();
                popup.close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Save failed", "Could not save the exam. Please try again.");
            }
        });

        popup.setScene(new Scene(content, 420, 360));
        popup.setResizable(false);
        popup.showAndWait();
    }

    private void selectSubject(ComboBox<Subject> subjectComboBox, int subjectId) {
        for (Subject subject : subjectComboBox.getItems()) {
            if (subject.getSubjectId() == subjectId) {
                subjectComboBox.getSelectionModel().select(subject);
                return;
            }
        }
    }

    private Exam buildExamFromForm(
            Exam existingExam,
            TextField titleField,
            DatePicker datePicker,
            TextField durationField,
            TextField totalMarksField,
            ComboBox<String> statusComboBox,
            ComboBox<Subject> subjectComboBox
    ) {
        String title = titleField.getText() == null ? "" : titleField.getText().trim();
        LocalDate examDate = datePicker.getValue();
        Subject subject = subjectComboBox.getValue();
        String status = statusComboBox.getValue();

        if (title.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing title", "Please enter an exam title.");
            titleField.requestFocus();
            return null;
        }

        if (examDate == null) {
            showAlert(Alert.AlertType.WARNING, "Missing date", "Please choose an exam date.");
            datePicker.requestFocus();
            return null;
        }

        if (subject == null) {
            showAlert(Alert.AlertType.WARNING, "Missing subject", "Please choose a subject.");
            subjectComboBox.requestFocus();
            return null;
        }

        int duration = parsePositiveInt(durationField, "Duration");
        if (duration <= 0) {
            return null;
        }

        int totalMarks = parsePositiveInt(totalMarksField, "Total marks");
        if (totalMarks <= 0) {
            return null;
        }

        UserInfo teacher = userInfoDAO.getById(currentTeacherId);
        if (teacher == null) {
            showAlert(Alert.AlertType.ERROR, "Teacher not found",
                    "Could not find the current teacher account for this exam.");
            return null;
        }

        Exam exam = existingExam == null ? new Exam() : existingExam;
        exam.setExamTitle(title);
        exam.setExamDate(examDate);
        exam.setDuration(duration);
        exam.setTotalMarks(totalMarks);
        exam.setStatus(status);
        exam.setSubject(subject);
        exam.setTeacher(teacher);
        return exam;
    }

    private int parsePositiveInt(TextField field, String label) {
        try {
            int value = Integer.parseInt(field.getText().trim());
            if (value <= 0) {
                showAlert(Alert.AlertType.WARNING, "Invalid " + label, label + " must be greater than 0.");
                field.requestFocus();
                return -1;
            }
            return value;
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Invalid " + label, label + " must be a number.");
            field.requestFocus();
            return -1;
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        if (examTable.getScene() != null) {
            alert.initOwner(examTable.getScene().getWindow());
        }
        alert.showAndWait();
    }
}
