package com.xuka.exam.controller;

import java.util.List;

import com.xuka.exam.dao.SubjectDAO;
import com.xuka.exam.models.Subject;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ManageSubjectsController {

    @FXML
    private TableView<Subject> subjectTable;

    @FXML
    private TableColumn<Subject, Integer> subjectIdColumn;

    @FXML
    private TableColumn<Subject, String> subjectCodeColumn;

    @FXML
    private TableColumn<Subject, String> subjectNameColumn;

    @FXML
    private TableColumn<Subject, String> subjectDescriptionColumn;

    @FXML
    private TableColumn<Subject, Void> subjectActionColumn;

    private final SubjectDAO subjectDAO = new SubjectDAO();

    @FXML
    private void initialize() {
        subjectIdColumn.setCellValueFactory(new PropertyValueFactory<>("subjectId"));
        subjectCodeColumn.setCellValueFactory(new PropertyValueFactory<>("subjectCode"));
        subjectNameColumn.setCellValueFactory(new PropertyValueFactory<>("subjectName"));
        subjectDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        setupActionColumn();
        loadSubjects();
    }

    private void loadSubjects() {
        try {
            List<Subject> subjects = subjectDAO.getAll();
            subjectTable.setItems(FXCollections.observableArrayList(subjects));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Load failed", "Could not load subjects from the database.");
            e.printStackTrace();
        }
    }

    private void setupActionColumn() {
        subjectActionColumn.setCellFactory(column -> new TableCell<>() {
            private final Button editButton = createIconButton(
                    "M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25z M20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34a.9959.9959 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z",
                    "Edit subject",
                    "#1976d2");
            private final Button deleteButton = createIconButton(
                    "M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12z M8 9h8v10H8V9z M15.5 4l-1-1h-5l-1 1H5v2h14V4z",
                    "Delete subject",
                    "#d32f2f");
            private final HBox actionBox = new HBox(6, editButton, deleteButton);

            {
                actionBox.setStyle("-fx-alignment: center;");
                editButton.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> event.consume());
                deleteButton.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> event.consume());

                editButton.setOnAction(event -> {
                    Subject subject = getSubjectForCurrentRow();
                    if (subject != null) {
                        subjectTable.getSelectionModel().select(subject);
                        showEditSubjectPopup(subject);
                    }
                });

                deleteButton.setOnAction(event -> {
                    Subject subject = getSubjectForCurrentRow();
                    if (subject != null) {
                        subjectTable.getSelectionModel().select(subject);
                        deleteSubject(subject);
                    }
                });
            }

            private Subject getSubjectForCurrentRow() {
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
    private void onAddSubjectButtonAction() {
        showAddSubjectPopup();
    }

    private void showAddSubjectPopup() {
        Stage popup = createSubjectPopup("Add Subject");

        TextField codeField = new TextField();
        codeField.setPromptText("Subject code");
        TextField nameField = new TextField();
        nameField.setPromptText("Subject name");
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Description");

        Button saveButton = new Button("Save");
        saveButton.setDefaultButton(true);
        saveButton.setStyle("-fx-background-color: #388e3c; -fx-text-fill: white; -fx-font-weight: bold;");

        VBox content = buildSubjectForm(popup, codeField, nameField, descriptionArea, saveButton);

        saveButton.setOnAction(event -> {
            Subject subject = buildSubjectFromForm(codeField, nameField, descriptionArea, null);
            if (subject == null) {
                return;
            }

            if (subjectDAO.save(subject)) {
                loadSubjects();
                subjectTable.getSelectionModel().select(subject);
                popup.close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Save failed", "Could not save the subject. Please try again.");
            }
        });

        popup.setScene(new Scene(content, 430, 320));
        popup.setResizable(false);
        popup.showAndWait();
    }

    private void showEditSubjectPopup(Subject subject) {
        Stage popup = createSubjectPopup("Edit Subject");

        TextField codeField = new TextField(subject.getSubjectCode());
        TextField nameField = new TextField(subject.getSubjectName());
        TextArea descriptionArea = new TextArea(subject.getDescription() == null ? "" : subject.getDescription());

        Button saveButton = new Button("Save");
        saveButton.setDefaultButton(true);
        saveButton.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-font-weight: bold;");

        VBox content = buildSubjectForm(popup, codeField, nameField, descriptionArea, saveButton);

        saveButton.setOnAction(event -> {
            Subject updatedSubject = buildSubjectFromForm(codeField, nameField, descriptionArea, subject);
            if (updatedSubject == null) {
                return;
            }

            if (subjectDAO.update(updatedSubject)) {
                loadSubjects();
                popup.close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Update failed", "Could not update the subject. Please try again.");
            }
        });

        popup.setScene(new Scene(content, 430, 320));
        popup.setResizable(false);
        popup.showAndWait();
    }

    private Stage createSubjectPopup(String title) {
        Stage popup = new Stage();
        popup.setTitle(title);
        popup.initModality(Modality.APPLICATION_MODAL);

        if (subjectTable.getScene() != null) {
            popup.initOwner(subjectTable.getScene().getWindow());
        }

        return popup;
    }

    private VBox buildSubjectForm(
            Stage popup,
            TextField codeField,
            TextField nameField,
            TextArea descriptionArea,
            Button saveButton
    ) {
        descriptionArea.setPrefRowCount(5);
        descriptionArea.setWrapText(true);

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.setPadding(new Insets(18));
        form.add(new Label("Code"), 0, 0);
        form.add(codeField, 1, 0);
        form.add(new Label("Name"), 0, 1);
        form.add(nameField, 1, 1);
        form.add(new Label("Description"), 0, 2);
        form.add(descriptionArea, 1, 2);
        GridPane.setHgrow(codeField, Priority.ALWAYS);
        GridPane.setHgrow(nameField, Priority.ALWAYS);
        GridPane.setHgrow(descriptionArea, Priority.ALWAYS);

        Button cancelButton = new Button("Cancel");
        cancelButton.setCancelButton(true);
        cancelButton.setOnAction(event -> popup.close());

        HBox actions = new HBox(10, cancelButton, saveButton);
        actions.setStyle("-fx-alignment: center-right;");

        VBox content = new VBox(8, form, actions);
        content.setPadding(new Insets(6, 6, 14, 6));
        return content;
    }

    private Subject buildSubjectFromForm(
            TextField codeField,
            TextField nameField,
            TextArea descriptionArea,
            Subject existingSubject
    ) {
        String code = codeField.getText() == null ? "" : codeField.getText().trim().toUpperCase();
        String name = nameField.getText() == null ? "" : nameField.getText().trim();
        String description = descriptionArea.getText() == null ? "" : descriptionArea.getText().trim();

        if (code.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing code", "Please enter a subject code.");
            codeField.requestFocus();
            return null;
        }

        if (code.length() > 20) {
            showAlert(Alert.AlertType.WARNING, "Invalid code", "Subject code must be 20 characters or fewer.");
            codeField.requestFocus();
            return null;
        }

        if (name.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing name", "Please enter a subject name.");
            nameField.requestFocus();
            return null;
        }

        if (name.length() > 150) {
            showAlert(Alert.AlertType.WARNING, "Invalid name", "Subject name must be 150 characters or fewer.");
            nameField.requestFocus();
            return null;
        }

        if (isDuplicateCode(code, existingSubject)) {
            showAlert(Alert.AlertType.WARNING, "Duplicate code", "A subject with this code already exists.");
            codeField.requestFocus();
            return null;
        }

        Subject subject = existingSubject == null ? new Subject() : existingSubject;
        subject.setSubjectCode(code);
        subject.setSubjectName(name);
        subject.setDescription(description);
        return subject;
    }

    private boolean isDuplicateCode(String code, Subject currentSubject) {
        Subject existingSubject = subjectDAO.getByCode(code);
        return existingSubject != null
                && (currentSubject == null || existingSubject.getSubjectId() != currentSubject.getSubjectId());
    }

    private void deleteSubject(Subject subject) {
        Alert confirmAlert = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Delete " + subject.getSubjectCode() + " - " + subject.getSubjectName()
                        + "?\nRelated exams, questions, and registrations may also be deleted.",
                ButtonType.CANCEL,
                ButtonType.OK
        );
        confirmAlert.setTitle("Delete Subject");
        confirmAlert.setHeaderText(null);
        if (subjectTable.getScene() != null) {
            confirmAlert.initOwner(subjectTable.getScene().getWindow());
        }

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        if (subjectDAO.delete(subject.getSubjectId())) {
            loadSubjects();
            showAlert(Alert.AlertType.INFORMATION, "Subject deleted", "Subject deleted successfully.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Delete failed", "Could not delete the subject. Please try again.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        if (subjectTable.getScene() != null) {
            alert.initOwner(subjectTable.getScene().getWindow());
        }
        alert.showAndWait();
    }
}
