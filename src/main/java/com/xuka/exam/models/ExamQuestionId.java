package com.xuka.exam.models;

import java.io.Serializable;
import java.util.Objects;

public class ExamQuestionId implements Serializable {
    private int exam;
    private int question;

    public ExamQuestionId() {
    }

    public ExamQuestionId(int exam, int question) {
        this.exam = exam;
        this.question = question;
    }

    public int getExam() {
        return exam;
    }

    public void setExam(int exam) {
        this.exam = exam;
    }

    public int getQuestion() {
        return question;
    }

    public void setQuestion(int question) {
        this.question = question;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExamQuestionId that = (ExamQuestionId) o;
        return exam == that.exam && question == that.question;
    }

    @Override
    public int hashCode() {
        return Objects.hash(exam, question);
    }
}
