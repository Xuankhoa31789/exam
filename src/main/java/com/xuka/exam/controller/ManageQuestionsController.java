package com.xuka.exam.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.xuka.exam.dao.ExamQuestionDAO;
import com.xuka.exam.dao.QuestionDAO;
import com.xuka.exam.models.Exam;
import com.xuka.exam.models.ExamQuestion;
import com.xuka.exam.models.Question;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ManageQuestionsController {

    private static final String WRITTEN = "Written";
    private static final String MULTIPLE_CHOICE = "Multiple-choice";
    private static final String CHOICES_HEADER = "Choices:";

    @FXML
    private Label titleLabel;

    @FXML
    private TableView<ExamQuestion> questionTable;

    @FXML
    private TableColumn<ExamQuestion, Integer> orderColumn;

    @FXML
    private TableColumn<ExamQuestion, String> questionTextColumn;

    @FXML
    private TableColumn<ExamQuestion, String> questionTypeColumn;

    @FXML
    private TableColumn<ExamQuestion, Integer> marksColumn;

    @FXML
    private TableColumn<ExamQuestion, String> correctAnswerColumn;

    @FXML
    private TableColumn<ExamQuestion, Void> questionActionColumn;

    private final QuestionDAO questionDAO = new QuestionDAO();
    private final ExamQuestionDAO examQuestionDAO = new ExamQuestionDAO();
    private Exam exam;

    @FXML
    private void initialize() {
        orderColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getQuestionOrder()).asObject());
        questionTextColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(getPromptText(cellData.getValue().getQuestion())));
        questionTypeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getQuestion().getQuestionType()));
        marksColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getQuestion().getMarks()).asObject());
        correctAnswerColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getQuestion().getCorrectAnswer()));

        setupActionColumn();
        setupRowClick();
    }

    public void setExam(Exam exam) {
        this.exam = exam;
        titleLabel.setText("Manage Questions - " + exam.getExamTitle());
        loadQuestions();
    }

    @FXML
    private void onAddQuestionButtonAction() {
        showQuestionPopup(null);
    }

    private void loadQuestions() {
        if (exam == null) {
            return;
        }

        try {
            questionTable.setItems(FXCollections.observableArrayList(examQuestionDAO.getByExam(exam.getExamId())));
        } catch (Exception e) {
            System.err.println("Error loading questions: " + e.getMessage());
            e.printStackTrace();
            questionTable.setItems(FXCollections.observableArrayList());
        }
    }

    private void setupActionColumn() {
        questionActionColumn.setCellFactory(column -> new TableCell<>() {
            private final Button editButton = createIconButton(
                    "M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25z M20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34a.9959.9959 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z",
                    "Edit question",
                    "#1976d2");
            private final Button deleteButton = createIconButton(
                    "M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12z M8 9h8v10H8V9z M15.5 4l-1-1h-5l-1 1H5v2h14V4z",
                    "Remove question",
                    "#d32f2f");
            private final HBox actionBox = new HBox(6, editButton, deleteButton);

            {
                actionBox.setStyle("-fx-alignment: center;");
                editButton.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> event.consume());
                deleteButton.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> event.consume());

                editButton.setOnAction(event -> {
                    ExamQuestion examQuestion = getExamQuestionForCurrentRow();
                    if (examQuestion != null) {
                        questionTable.getSelectionModel().select(examQuestion);
                        showQuestionPopup(examQuestion.getQuestion());
                    }
                });

                deleteButton.setOnAction(event -> {
                    ExamQuestion examQuestion = getExamQuestionForCurrentRow();
                    if (examQuestion != null) {
                        questionTable.getSelectionModel().select(examQuestion);
                        removeQuestion(examQuestion);
                    }
                });
            }

            private ExamQuestion getExamQuestionForCurrentRow() {
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

    private void setupRowClick() {
        questionTable.setRowFactory(tableView -> {
            TableRow<ExamQuestion> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    showQuestionPopup(row.getItem().getQuestion());
                }
            });
            return row;
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

    private void showQuestionPopup(Question question) {
        Stage popup = new Stage();
        popup.setTitle(question == null ? "Add Question" : "Edit Question");
        popup.initModality(Modality.APPLICATION_MODAL);
        if (questionTable.getScene() != null) {
            popup.initOwner(questionTable.getScene().getWindow());
        }

        TextArea questionTextArea = new TextArea(question == null ? "" : getPromptText(question));
        questionTextArea.setPromptText("Question text");
        questionTextArea.setPrefRowCount(4);
        questionTextArea.setWrapText(true);

        ComboBox<String> typeComboBox = new ComboBox<>(FXCollections.observableArrayList(WRITTEN, MULTIPLE_CHOICE));
        typeComboBox.setValue(question == null ? WRITTEN : normalizeQuestionType(question.getQuestionType()));

        TextField marksField = new TextField(question == null ? "1" : String.valueOf(question.getMarks()));
        marksField.setPromptText("Marks");

        VBox answerBox = new VBox(10);
        answerBox.setPadding(new Insets(4, 0, 0, 0));

        Label questionLabel = new Label("Question");
        Runnable rebuildAnswerBox = () -> rebuildAnswerBox(answerBox, typeComboBox.getValue(), question, questionLabel, questionTextArea);
        typeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> rebuildAnswerBox.run());
        rebuildAnswerBox.run();

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.setPadding(new Insets(18));
        form.setMinWidth(560);

        ColumnConstraints labelColumn = new ColumnConstraints();
        labelColumn.setMinWidth(95);
        labelColumn.setPrefWidth(95);

        ColumnConstraints inputColumn = new ColumnConstraints();
        inputColumn.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(labelColumn, inputColumn);

        form.add(questionLabel, 0, 0);
        form.add(questionTextArea, 1, 0);
        form.add(new Label("Type"), 0, 1);
        form.add(typeComboBox, 1, 1);
        form.add(new Label("Marks"), 0, 2);
        form.add(marksField, 1, 2);
        form.add(answerBox, 1, 3);

        Button saveButton = new Button("Save");
        saveButton.setDefaultButton(true);
        saveButton.setStyle("-fx-background-color: #6a1b9a; -fx-text-fill: white; -fx-font-weight: bold;");

        Button cancelButton = new Button("Cancel");
        cancelButton.setCancelButton(true);
        cancelButton.setOnAction(event -> popup.close());

        HBox actions = new HBox(10, cancelButton, saveButton);
        actions.setStyle("-fx-alignment: center-right;");

        ScrollPane formScrollPane = new ScrollPane(form);
        formScrollPane.setFitToWidth(true);
        VBox.setVgrow(formScrollPane, Priority.ALWAYS);
        formScrollPane.setPrefViewportHeight(420);

        VBox content = new VBox(8, formScrollPane, actions);
        content.setPadding(new Insets(6, 6, 14, 6));

        saveButton.setOnAction(event -> {
            if (saveQuestion(question, questionTextArea, typeComboBox, marksField, answerBox)) {
                popup.close();
            }
        });

        popup.setScene(new Scene(content, 640, 560));
        popup.setResizable(false);
        popup.showAndWait();
    }

    private void rebuildAnswerBox(VBox answerBox, String questionType, Question question, Label questionLabel, TextArea questionTextArea) {
        answerBox.getChildren().clear();
        boolean batchWrittenAdd = question == null && WRITTEN.equals(questionType);
        questionLabel.setVisible(!batchWrittenAdd);
        questionLabel.setManaged(!batchWrittenAdd);
        questionTextArea.setVisible(!batchWrittenAdd);
        questionTextArea.setManaged(!batchWrittenAdd);

        if (WRITTEN.equals(questionType)) {
            if (batchWrittenAdd) {
                VBox writtenQuestionsBox = new VBox(10);
                writtenQuestionsBox.setId("writtenQuestionsBox");
                addWrittenQuestionField(writtenQuestionsBox, "", "");

                Button addQuestionButton = new Button("Add Question");
                addQuestionButton.setOnAction(event -> addWrittenQuestionField(writtenQuestionsBox, "", ""));

                answerBox.getChildren().addAll(
                        new Label("Written Questions"),
                        writtenQuestionsBox,
                        addQuestionButton
                );
                return;
            }

            TextArea answerArea = new TextArea(question == null ? "" : safe(question.getCorrectAnswer()));
            answerArea.setId("writtenAnswerArea");
            answerArea.setPromptText("Correct answer");
            answerArea.setPrefRowCount(3);
            answerArea.setWrapText(true);
            answerBox.getChildren().addAll(
                    new Label("Answer"),
                    answerArea
            );
            return;
        }

        VBox choicesBox = new VBox(8);
        choicesBox.setId("choicesBox");

        List<String> existingChoices = question == null ? List.of("", "") : getChoices(question.getQuestionText());
        if (existingChoices.size() < 2) {
            existingChoices = List.of("", "");
        }

        List<String> selectedAnswers = parseStoredAnswerKeys(question == null ? "" : question.getCorrectAnswer());
        for (int i = 0; i < existingChoices.size(); i++) {
            addChoiceField(choicesBox, existingChoices.get(i), selectedAnswers.contains(getChoiceLabel(i)));
        }

        Button addChoiceButton = new Button("Add Choice");
        addChoiceButton.setOnAction(event -> {
            if (choicesBox.getChildren().size() >= 26) {
                showAlert(Alert.AlertType.WARNING, "Too many choices", "Multiple-choice questions can have up to 26 choices.");
                return;
            }
            addChoiceField(choicesBox, "", false);
        });

        answerBox.getChildren().addAll(
                new Label("Choices"),
                choicesBox,
                addChoiceButton
        );
    }

    private void addChoiceField(VBox choicesBox, String value, boolean selected) {
        int choiceIndex = choicesBox.getChildren().size();
        Label label = new Label(getChoiceLabel(choiceIndex) + ".");
        label.setMinWidth(24);

        TextField choiceField = new TextField(value);
        choiceField.setPromptText("Choice " + getChoiceLabel(choiceIndex));
        choiceField.setPrefWidth(330);

        ToggleButton answerToggle = new ToggleButton("Answer");
        answerToggle.setSelected(selected);
        answerToggle.setMinWidth(76);
        answerToggle.setStyle("-fx-cursor: hand;");
        answerToggle.selectedProperty().addListener((observable, wasSelected, isSelected) -> updateAnswerToggleStyle(answerToggle));
        updateAnswerToggleStyle(answerToggle);

        Button removeButton = new Button("-");
        removeButton.setOnAction(event -> {
            if (choicesBox.getChildren().size() <= 2) {
                showAlert(Alert.AlertType.WARNING, "Not enough choices", "Multiple-choice questions need at least two choices.");
                return;
            }
            choicesBox.getChildren().remove(removeButton.getParent());
            refreshChoiceLabels(choicesBox);
        });

        HBox row = new HBox(8, label, choiceField, answerToggle, removeButton);
        row.setStyle("-fx-alignment: center-left;");
        choicesBox.getChildren().add(row);
        refreshChoiceLabels(choicesBox);
    }

    private void addWrittenQuestionField(VBox writtenQuestionsBox, String questionValue, String answerValue) {
        Label label = new Label((writtenQuestionsBox.getChildren().size() + 1) + ".");
        label.setMinWidth(24);

        TextArea questionArea = new TextArea(questionValue);
        questionArea.setPromptText("Question text");
        questionArea.setPrefRowCount(2);
        questionArea.setWrapText(true);
        HBox.setHgrow(questionArea, Priority.ALWAYS);

        TextArea answerArea = new TextArea(answerValue);
        answerArea.setPromptText("Correct answer");
        answerArea.setPrefRowCount(2);
        answerArea.setWrapText(true);
        HBox.setHgrow(answerArea, Priority.ALWAYS);

        Button removeButton = new Button("-");
        removeButton.setOnAction(event -> {
            if (writtenQuestionsBox.getChildren().size() <= 1) {
                showAlert(Alert.AlertType.WARNING, "Not enough questions", "Please keep at least one written question.");
                return;
            }
            writtenQuestionsBox.getChildren().remove(removeButton.getParent());
            refreshWrittenQuestionLabels(writtenQuestionsBox);
        });

        VBox fields = new VBox(6, questionArea, answerArea);
        HBox.setHgrow(fields, Priority.ALWAYS);

        HBox row = new HBox(8, label, fields, removeButton);
        row.setStyle("-fx-alignment: top-left;");
        writtenQuestionsBox.getChildren().add(row);
        refreshWrittenQuestionLabels(writtenQuestionsBox);
    }

    private void refreshWrittenQuestionLabels(VBox writtenQuestionsBox) {
        for (int i = 0; i < writtenQuestionsBox.getChildren().size(); i++) {
            HBox row = (HBox) writtenQuestionsBox.getChildren().get(i);
            Label label = (Label) row.getChildren().get(0);
            VBox fields = (VBox) row.getChildren().get(1);
            TextArea questionArea = (TextArea) fields.getChildren().get(0);
            TextArea answerArea = (TextArea) fields.getChildren().get(1);
            label.setText((i + 1) + ".");
            questionArea.setPromptText("Question text " + (i + 1));
            answerArea.setPromptText("Correct answer " + (i + 1));
        }
    }

    private void refreshChoiceLabels(VBox choicesBox) {
        for (int i = 0; i < choicesBox.getChildren().size(); i++) {
            HBox row = (HBox) choicesBox.getChildren().get(i);
            Label label = (Label) row.getChildren().get(0);
            TextField field = (TextField) row.getChildren().get(1);
            ToggleButton answerToggle = (ToggleButton) row.getChildren().get(2);
            label.setText(getChoiceLabel(i) + ".");
            field.setPromptText("Choice " + getChoiceLabel(i));
            answerToggle.setTooltip(new Tooltip("Mark choice " + getChoiceLabel(i) + " as correct"));
        }
    }

    private void updateAnswerToggleStyle(ToggleButton toggleButton) {
        if (toggleButton.isSelected()) {
            toggleButton.setStyle("-fx-background-color: #388e3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        } else {
            toggleButton.setStyle("-fx-background-color: #eeeeee; -fx-text-fill: #333333; -fx-cursor: hand;");
        }
    }

    private boolean saveQuestion(Question question, TextArea questionTextArea, ComboBox<String> typeComboBox, TextField marksField, VBox answerBox) {
        String prompt = questionTextArea.getText() == null ? "" : questionTextArea.getText().trim();
        String questionType = typeComboBox.getValue();

        int marks = parsePositiveInt(marksField, "Marks");
        if (marks <= 0) {
            return false;
        }

        if (question == null && WRITTEN.equals(questionType)) {
            VBox writtenQuestionsBox = (VBox) answerBox.lookup("#writtenQuestionsBox");
            List<WrittenQuestionInput> writtenQuestions = readWrittenQuestions(writtenQuestionsBox);
            if (writtenQuestions == null) {
                return false;
            }

            int nextOrder = getNextQuestionOrder();
            for (WrittenQuestionInput writtenQuestion : writtenQuestions) {
                if (!saveNewQuestion(writtenQuestion.questionText(), questionType, marks, writtenQuestion.correctAnswer(), nextOrder)) {
                    return false;
                }
                nextOrder++;
            }

            loadQuestions();
            return true;
        }

        if (prompt.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing question", "Please enter the question text.");
            questionTextArea.requestFocus();
            return false;
        }

        String questionText;
        String correctAnswer;

        if (WRITTEN.equals(questionType)) {
            TextArea answerArea = (TextArea) answerBox.lookup("#writtenAnswerArea");
            correctAnswer = answerArea.getText() == null ? "" : answerArea.getText().trim();
            if (correctAnswer.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Missing answer", "Please enter the correct answer.");
                answerArea.requestFocus();
                return false;
            }
            questionText = prompt;
        } else {
            VBox choicesBox = (VBox) answerBox.lookup("#choicesBox");
            List<String> choices = readChoices(choicesBox);
            if (choices == null) {
                return false;
            }

            correctAnswer = readSelectedAnswerKeys(choicesBox);
            if (correctAnswer == null) {
                return false;
            }

            questionText = buildMultipleChoiceQuestionText(prompt, choices);
        }

        if (question == null) {
            if (!saveNewQuestion(questionText, questionType, marks, correctAnswer, getNextQuestionOrder())) {
                return false;
            }
        } else {
            question.setQuestionText(questionText);
            question.setQuestionType(questionType);
            question.setMarks(marks);
            question.setCorrectAnswer(correctAnswer);
            if (!questionDAO.update(question)) {
                showAlert(Alert.AlertType.ERROR, "Update failed", "Could not update the question. Please try again.");
                return false;
            }
        }

        loadQuestions();
        return true;
    }

    private boolean saveNewQuestion(String questionText, String questionType, int marks, String correctAnswer, int questionOrder) {
        Question newQuestion = new Question(questionText, questionType, marks, correctAnswer, exam.getSubject());
        if (!questionDAO.save(newQuestion)) {
            showAlert(Alert.AlertType.ERROR, "Save failed", "Could not save the question. Please try again.");
            return false;
        }

        if (!examQuestionDAO.saveByIds(exam.getExamId(), newQuestion.getQuestionId(), questionOrder)) {
            questionDAO.delete(newQuestion.getQuestionId());
            showAlert(Alert.AlertType.ERROR, "Save failed", "Could not attach the question to this exam.");
            return false;
        }

        return true;
    }

    private int getNextQuestionOrder() {
        return questionTable.getItems().stream()
                .map(ExamQuestion::getQuestionOrder)
                .max(Comparator.naturalOrder())
                .orElse(0) + 1;
    }

    private List<WrittenQuestionInput> readWrittenQuestions(VBox writtenQuestionsBox) {
        List<WrittenQuestionInput> writtenQuestions = new ArrayList<>();
        for (int i = 0; i < writtenQuestionsBox.getChildren().size(); i++) {
            HBox row = (HBox) writtenQuestionsBox.getChildren().get(i);
            VBox fields = (VBox) row.getChildren().get(1);
            TextArea questionArea = (TextArea) fields.getChildren().get(0);
            TextArea answerArea = (TextArea) fields.getChildren().get(1);
            String questionText = questionArea.getText() == null ? "" : questionArea.getText().trim();
            String answer = answerArea.getText() == null ? "" : answerArea.getText().trim();
            if (questionText.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Missing question", "Please fill in question " + (i + 1) + ".");
                questionArea.requestFocus();
                return null;
            }
            if (answer.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Missing answer", "Please fill in correct answer " + (i + 1) + ".");
                answerArea.requestFocus();
                return null;
            }
            writtenQuestions.add(new WrittenQuestionInput(questionText, answer));
        }

        if (writtenQuestions.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing question", "Please add at least one written question.");
            return null;
        }

        return writtenQuestions;
    }

    private List<String> readChoices(VBox choicesBox) {
        List<String> choices = new ArrayList<>();
        for (int i = 0; i < choicesBox.getChildren().size(); i++) {
            HBox row = (HBox) choicesBox.getChildren().get(i);
            TextField field = (TextField) row.getChildren().get(1);
            String choice = field.getText() == null ? "" : field.getText().trim();
            if (choice.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Missing choice", "Please fill in choice " + getChoiceLabel(i) + ".");
                field.requestFocus();
                return null;
            }
            choices.add(choice);
        }

        if (choices.size() < 2) {
            showAlert(Alert.AlertType.WARNING, "Not enough choices", "Please add at least two choices.");
            return null;
        }

        return choices;
    }

    private String readSelectedAnswerKeys(VBox choicesBox) {
        List<String> selectedKeys = new ArrayList<>();
        for (int i = 0; i < choicesBox.getChildren().size(); i++) {
            HBox row = (HBox) choicesBox.getChildren().get(i);
            ToggleButton answerToggle = (ToggleButton) row.getChildren().get(2);
            if (answerToggle.isSelected()) {
                selectedKeys.add(getChoiceLabel(i));
            }
        }

        if (selectedKeys.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing answer", "Please turn on at least one answer toggle.");
            return null;
        }

        return String.join(",", selectedKeys);
    }

    private List<String> parseStoredAnswerKeys(String rawValue) {
        List<String> keys = new ArrayList<>();
        String raw = rawValue == null ? "" : rawValue.trim().toUpperCase();
        if (raw.isEmpty()) {
            return keys;
        }

        for (String key : raw.split("[,\\s]+")) {
            if (key.length() == 1 && key.charAt(0) >= 'A' && key.charAt(0) <= 'Z' && !keys.contains(key)) {
                keys.add(key);
            }
        }
        return keys;
    }

    private void removeQuestion(ExamQuestion examQuestion) {
        Question question = examQuestion.getQuestion();
        Alert confirmAlert = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Remove this question from the exam?",
                ButtonType.CANCEL,
                ButtonType.OK
        );
        confirmAlert.setTitle("Remove Question");
        confirmAlert.setHeaderText(null);
        if (questionTable.getScene() != null) {
            confirmAlert.initOwner(questionTable.getScene().getWindow());
        }

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        if (examQuestionDAO.delete(exam.getExamId(), question.getQuestionId())) {
            questionDAO.delete(question.getQuestionId());
            loadQuestions();
        } else {
            showAlert(Alert.AlertType.ERROR, "Remove failed", "Could not remove the question. Please try again.");
        }
    }

    private String buildMultipleChoiceQuestionText(String prompt, List<String> choices) {
        StringBuilder builder = new StringBuilder(prompt.trim());
        builder.append("\n\n").append(CHOICES_HEADER);
        for (int i = 0; i < choices.size(); i++) {
            builder.append("\n").append(getChoiceLabel(i)).append(". ").append(choices.get(i));
        }
        return builder.toString();
    }

    private String getPromptText(Question question) {
        if (question == null || question.getQuestionText() == null) {
            return "";
        }

        int choicesIndex = question.getQuestionText().indexOf("\n" + CHOICES_HEADER);
        if (choicesIndex < 0) {
            choicesIndex = question.getQuestionText().indexOf("\r\n" + CHOICES_HEADER);
        }
        if (choicesIndex < 0) {
            return question.getQuestionText();
        }
        return question.getQuestionText().substring(0, choicesIndex).trim();
    }

    private List<String> getChoices(String questionText) {
        List<String> choices = new ArrayList<>();
        if (questionText == null) {
            return choices;
        }

        boolean inChoices = false;
        for (String line : questionText.split("\\R")) {
            String trimmedLine = line.trim();
            if (CHOICES_HEADER.equals(trimmedLine)) {
                inChoices = true;
                continue;
            }
            if (inChoices && trimmedLine.length() >= 3 && Character.isLetter(trimmedLine.charAt(0)) && trimmedLine.charAt(1) == '.') {
                choices.add(trimmedLine.substring(2).trim());
            }
        }
        return choices;
    }

    private String normalizeQuestionType(String questionType) {
        if (MULTIPLE_CHOICE.equalsIgnoreCase(safe(questionType).trim())) {
            return MULTIPLE_CHOICE;
        }
        return WRITTEN;
    }

    private String getChoiceLabel(int index) {
        return String.valueOf((char) ('A' + index));
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

    private record WrittenQuestionInput(String questionText, String correctAnswer) {
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        if (questionTable.getScene() != null) {
            alert.initOwner(questionTable.getScene().getWindow());
        }
        alert.showAndWait();
    }
}
