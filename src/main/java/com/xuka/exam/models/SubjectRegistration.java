package com.xuka.exam.models;

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * SubjectRegistration entity represents the junction table for student-subject enrollment
 * Maps to the subject_registration table in the database
 */
@Entity
@Table(name = "subject_registration")
@IdClass(SubjectRegistrationId.class)
public class SubjectRegistration implements Serializable {
    @Id
    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Id
    @ManyToOne
    @JoinColumn(name = "uc_info_id", nullable = false)
    private UserInfo userInfo;

    // Constructors
    public SubjectRegistration() {
    }

    public SubjectRegistration(Subject subject, UserInfo userInfo) {
        this.subject = subject;
        this.userInfo = userInfo;
    }

    // Getters and Setters
    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    @Override
    public String toString() {
        return "SubjectRegistration{" +
                "subject=" + subject +
                ", userInfo=" + userInfo +
                '}';
    }
}
