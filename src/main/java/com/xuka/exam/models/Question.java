package com.xuka.exam.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "question")
public class Question {
    @Id
    @Column(name = "question_id", length = 20)
    private String questionId;

    @Column(name = "question_text", columnDefinition = "TEXT", nullable = false)
    private String questionText;

    @Column(name = "question_type", length = 50)
    private String questionType;

    @Column(name = "marks")
    private int marks;

    @Column(name = "correct_answer", length = 500)
    private String correctAnswer;

    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    // Constructors
    public Question() {
    }

    public Question(String questionId, String questionText, Subject subject) {
        this.questionId = questionId;
        this.questionText = questionText;
        this.subject = subject;
    }

    public Question(String questionId, String questionText, String questionType, int marks, String correctAnswer, Subject subject) {
        this.questionId = questionId;
        this.questionText = questionText;
        this.questionType = questionType;
        this.marks = marks;
        this.correctAnswer = correctAnswer;
        this.subject = subject;
    }

    // Getters and Setters
    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public int getMarks() {
        return marks;
    }

    public void setMarks(int marks) {
        this.marks = marks;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    @Override
    public String toString() {
        return "Question{" +
                "questionId='" + questionId + '\'' +
                ", questionText='" + questionText + '\'' +
                ", questionType='" + questionType + '\'' +
                ", marks=" + marks +
                ", correctAnswer='" + correctAnswer + '\'' +
                ", subject=" + subject +
                '}';
    }
}
