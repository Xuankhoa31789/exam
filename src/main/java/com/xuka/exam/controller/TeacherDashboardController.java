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
import com.xuka.exam.models.Exam;
import com.xuka.exam.models.ExamAttempt;
import com.xuka.exam.models.Subject;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
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
    private int currentTeacherId = 1; // This should be set from login session

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        examDAO = new ExamDAO();
        examAttemptDAO = new ExamAttemptDAO();

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
