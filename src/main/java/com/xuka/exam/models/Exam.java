package com.xuka.exam.models;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "Exam")
public class Exam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exam_id")
    private int examId;

    @Column(name = "exam_title", nullable = false, length = 200)
    private String examTitle;

    @Column(name = "exam_date", nullable = false)
    private LocalDate examDate;

    @Column(name = "duration", nullable = false)
    private int duration; // in minutes

    @Column(name = "total_marks", nullable = false)
    private int totalMarks;

    @Column(name = "status", length = 50)
    private String status; // e.g., Scheduled, Ongoing, Completed

    @Column(name = "exam_data", columnDefinition = "TEXT")
    private String examData;

    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne
    @JoinColumn(name = "teacher_uc_info_id", nullable = false)
    private UserInfo teacher;

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ExamQuestion> examQuestions;

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ExamAttempt> examAttempts;

    // Constructors
    public Exam() {
    }

    public Exam(String examTitle, LocalDate examDate, int duration, int totalMarks, Subject subject, UserInfo teacher) {
        this.examTitle = examTitle;
        this.examDate = examDate;
        this.duration = duration;
        this.totalMarks = totalMarks;
        this.subject = subject;
        this.teacher = teacher;
    }

    public Exam(String examTitle, LocalDate examDate, int duration, int totalMarks, String status, Subject subject, UserInfo teacher) {
        this.examTitle = examTitle;
        this.examDate = examDate;
        this.duration = duration;
        this.totalMarks = totalMarks;
        this.status = status;
        this.subject = subject;
        this.teacher = teacher;
    }

    // Getters and Setters
    public int getExamId() {
        return examId;
    }

    public void setExamId(int examId) {
        this.examId = examId;
    }

    public String getExamTitle() {
        return examTitle;
    }

    public void setExamTitle(String examTitle) {
        this.examTitle = examTitle;
    }

    public LocalDate getExamDate() {
        return examDate;
    }

    public void setExamDate(LocalDate examDate) {
        this.examDate = examDate;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(int totalMarks) {
        this.totalMarks = totalMarks;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getExamData() {
        return examData;
    }

    public void setExamData(String examData) {
        this.examData = examData;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public UserInfo getTeacher() {
        return teacher;
    }

    public void setTeacher(UserInfo teacher) {
        this.teacher = teacher;
    }

    public List<ExamQuestion> getExamQuestions() {
        return examQuestions;
    }

    public void setExamQuestions(List<ExamQuestion> examQuestions) {
        this.examQuestions = examQuestions;
    }

    public List<ExamAttempt> getExamAttempts() {
        return examAttempts;
    }

    public void setExamAttempts(List<ExamAttempt> examAttempts) {
        this.examAttempts = examAttempts;
    }


    @Override
    public String toString() {
        return "Exam{" +
                "examId=" + examId +
                ", examTitle='" + examTitle + '\'' +
                ", examDate=" + examDate +
                ", duration=" + duration +
                ", totalMarks=" + totalMarks +
                ", status='" + status + '\'' +
                ", examData='" + examData + '\'' +
                '}';
    }
}
