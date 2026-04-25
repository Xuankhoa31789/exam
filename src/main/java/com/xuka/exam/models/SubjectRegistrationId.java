package com.xuka.exam.models;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite key for SubjectRegistration entity
 * Represents the combination of subject_id and uc_info_id
 */
public class SubjectRegistrationId implements Serializable {
    private int subject;
    private int userInfo;

    public SubjectRegistrationId() {
    }

    public SubjectRegistrationId(int subject, int userInfo) {
        this.subject = subject;
        this.userInfo = userInfo;
    }

    public int getSubject() {
        return subject;
    }

    public void setSubject(int subject) {
        this.subject = subject;
    }

    public int getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(int userInfo) {
        this.userInfo = userInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubjectRegistrationId that = (SubjectRegistrationId) o;
        return subject == that.subject && userInfo == that.userInfo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(subject, userInfo);
    }

    @Override
    public String toString() {
        return "SubjectRegistrationId{" +
                "subject=" + subject +
                ", userInfo=" + userInfo +
                '}';
    }
}
