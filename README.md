# 📚 Student Exam Management System

> A full-featured JavaFX desktop application for managing exams, questions, subjects, and student performance — with role-based dashboards for Teachers and Students.

![Version](https://img.shields.io/badge/version-1.0-blue.svg)
![Java](https://img.shields.io/badge/Java-23+-orange.svg)
![JavaFX](https://img.shields.io/badge/JavaFX-17.0.6-green.svg)
![Hibernate](https://img.shields.io/badge/Hibernate-7.3.1-red.svg)
![MySQL](https://img.shields.io/badge/MySQL-8.0+-blue.svg)
![Status](https://img.shields.io/badge/status-functional-brightgreen.svg)

---

## 🎯 Overview

The **Student Exam Management System** is a desktop application built with JavaFX and backed by a MySQL database via Hibernate ORM. It supports two user roles — **Teacher** and **Student** — each with their own dashboard and feature set. Authentication uses SHA-256 password hashing with per-user salts.

---

## ✨ Features

### 🔐 Authentication
- Login with username and password (SHA-256 + salt hashing via `PasswordUtil`)
- Self-registration as Student or Teacher
- Role-based routing to the correct dashboard after login
- Profile view, edit, password change, and account deletion from within the app

### 👨‍🏫 Teacher Dashboard
- **Summary cards**: Total exams, completed exams, scheduled exams, average score, active students
- **Pie chart**: Exam status distribution (Scheduled / Ongoing / Completed)
- **Bar chart**: Average scores per exam across recent attempts
- **Recent attempts table**: Student name, exam, start/end time, score, status, duration
- **Manage Exams**: Create, edit, and delete exams (title, date, duration, marks, status, subject)
- **Manage Questions**: Add written or multiple-choice questions to scheduled exams; supports batch add, inline choice editor, toggleable correct-answer selection
- **Manage Subjects**: Create, edit, and delete subjects (code, name, description) with duplicate-code validation

### 🎓 Student Dashboard
- **Summary cards**: Enrolled subjects, available exams, completed attempts, average score
- **Subject registration**: Browse and register for subjects; filter exam list by subject
- **Available exams table**: Shows all exams for enrolled subjects with status
- **Take exam**: Opens an inline exam window with all questions; auto-scores on submission
- **Attempts history**: Full list of past attempts with score, timing, and status

---

## 📁 Project Structure

```
exam/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/xuka/exam/
│   │   │       ├── ExamApplication.java              # JavaFX entry point
│   │   │       ├── config/
│   │   │       │   └── HibernateUtil.java            # Singleton EntityManagerFactory
│   │   │       ├── controller/
│   │   │       │   ├── LoginController.java
│   │   │       │   ├── RegisterController.java
│   │   │       │   ├── TeacherDashboardController.java
│   │   │       │   ├── StudentDashboardController.java
│   │   │       │   ├── ManageExamsController.java
│   │   │       │   ├── ManageQuestionsController.java
│   │   │       │   └── ManageSubjectsController.java
│   │   │       ├── dao/
│   │   │       │   ├── UserAccountDAO.java
│   │   │       │   ├── UserInfoDAO.java
│   │   │       │   ├── SubjectDAO.java
│   │   │       │   ├── SubjectRegistrationDAO.java
│   │   │       │   ├── ExamDAO.java
│   │   │       │   ├── ExamQuestionDAO.java
│   │   │       │   ├── ExamAttemptDAO.java
│   │   │       │   ├── QuestionDAO.java
│   │   │       │   └── StudentAnswerDAO.java
│   │   │       ├── models/
│   │   │       │   ├── UserAccount.java
│   │   │       │   ├── UserInfo.java
│   │   │       │   ├── Subject.java
│   │   │       │   ├── Question.java
│   │   │       │   ├── Exam.java
│   │   │       │   ├── ExamQuestion.java / ExamQuestionId.java
│   │   │       │   ├── ExamAttempt.java
│   │   │       │   ├── StudentAnswer.java
│   │   │       │   └── SubjectRegistration.java / SubjectRegistrationId.java
│   │   │       └── util/
│   │   │           └── PasswordUtil.java             # SHA-256 + salt hashing
│   │   └── resources/
│   │       ├── com/xuka/exam/
│   │       │   ├── login_screen.fxml
│   │       │   ├── register_screen.fxml
│   │       │   ├── teacher_dashboard.fxml
│   │       │   ├── student_dashboard.fxml
│   │       │   ├── manage_exams_popup.fxml
│   │       │   ├── manage_questions_popup.fxml
│   │       │   └── manage_subjects_popup.fxml
│   │       └── META-INF/
│   │           └── persistence.xml                   # JPA/Hibernate configuration
│   └── test/
│       └── java/
├── pom.xml
└── README.md
```

---

## 🗄️ Data Model

```
UserAccount ──────── UserInfo
  (username,           (fullName, email,
   pwd_hash, salt,      phone, dob,
   role 0/1)            department, code)
                            │
              ┌─────────────┴─────────────┐
              │                           │
    SubjectRegistration           ExamAttempt
    (student ↔ subject)           (student, exam,
                                   score, status)
                                       │
                                 StudentAnswer
                                 (answerText,
                                  obtainedMarks)

Subject ──── Exam ──── ExamQuestion ──── Question
(code,       (title,   (composite PK:    (text, type,
 name,        date,     exam+question,    marks,
 desc)        duration, order)            correctAnswer)
              marks,
              status,
              teacher → UserInfo)
```

**Roles**: `role = 0` → Student, `role = 1` → Teacher

---

## 🏗️ Architecture

The application follows the **MVC pattern** with a dedicated **DAO layer**:

```
┌────────────────────────┐
│   View (FXML)          │  ← login, register, teacher/student dashboards,
│                        │    manage_exams/questions/subjects popups
└──────────┬─────────────┘
           │
┌──────────▼─────────────┐
│   Controller Layer     │  ← Input validation, navigation, UI logic
│   (7 controllers)      │    Runs DB ops on background threads (Platform.runLater)
└──────────┬─────────────┘
           │
┌──────────▼─────────────┐
│   DAO Layer (9 DAOs)   │  ← All Hibernate EntityManager operations,
│                        │    transactions with rollback on failure
└──────────┬─────────────┘
           │
┌──────────▼─────────────┐
│   Model Layer          │  ← Jakarta Persistence entities with
│   (9 entities)         │    @ManyToOne, @OneToMany, composite PKs
└────────────────────────┘
```

---

## 📦 Prerequisites

| Requirement | Version |
|---|---|
| JDK | 23 or higher |
| Maven | 3.6 or later |
| MySQL | 8.0 or later |

---

## ⚙️ Database Setup

Before running the application, create the database and configure credentials.

**1. Create the database:**

```sql
CREATE DATABASE exam_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

**2. Configure credentials in `src/main/resources/META-INF/persistence.xml`:**

```xml
<property name="jakarta.persistence.jdbc.url"      value="jdbc:mysql://localhost:3306/exam_management"/>
<property name="jakarta.persistence.jdbc.user"     value="root"/>
<property name="jakarta.persistence.jdbc.password" value="your_password"/>
```

Hibernate is set to `hbm2ddl.auto = update`, so tables are created automatically on first run.

---

## 🚀 Quick Start

**Clone and run in three steps:**

```bash
# 1. Clone the repository
git clone https://github.com/Xuankhoa31789/exam.git
cd exam

# 2. Install dependencies
mvn clean install

# 3. Run the application
mvn javafx:run
```

The login screen will open. Register a new account (as Teacher or Student), then log in.

---

## 🛠️ Build Commands

```bash
# Clean build
mvn clean package

# Run in development mode (recommended)
mvn javafx:run

# Run tests
mvn clean verify
```

---

## 📊 Technology Stack

| Technology | Version | Purpose |
|---|---|---|
| Java | 23+ | Language |
| JavaFX | 17.0.6 | GUI framework (controls, FXML, charts) |
| Hibernate ORM | 7.3.1 | Object-relational mapping |
| Jakarta Persistence | 3.2.0 | JPA API |
| MySQL Connector/J | 9.6.0 | Database driver |
| FormsFX | 11.6.0 | JavaFX form utilities |
| Ikonli | 12.3.1 | Icon support |
| SLF4J Simple | 2.0.17 | Logging |
| JUnit Jupiter | 5.10.2 | Testing |
| Maven | 3.6+ | Build automation |

---

## 🔒 Security

- Passwords are never stored in plain text
- Each account receives a unique randomly generated salt (32 bytes, Base64-encoded via `SecureRandom`)
- Passwords are hashed with SHA-256 applied over `salt + password`
- Verification re-hashes the input and compares — no decryption involved

---

## 🗺️ Screens & Navigation

```
Login Screen
  ├── [Login]    → Teacher Dashboard  (role = 1)
  │                  ├── Manage Exams    (popup)
  │                  ├── Manage Questions (popup, per scheduled exam)
  │                  └── Manage Subjects  (popup)
  └── [Register] → Register Screen
                     └── [Back] → Login Screen

Login Screen
  └── [Login]    → Student Dashboard  (role = 0)
                     ├── Register / Select / Unregister Subject
                     ├── Take Exam (inline exam window)
                     └── View Attempt History
```

---

## 🐛 Known Issues & Limitations

- SQL logging is enabled by default (`hibernate.show_sql = true`) — disable in production via `persistence.xml`
- No timer enforcement during exams; students can take as long as they want
- Multiple-choice answer scoring is exact-match only (correct key(s) stored as comma-separated letter codes, e.g. `A,C`)
- Written question scoring is exact-match string comparison (case-insensitive)
- No admin role or global user management panel yet

---

## 📧 Contact

- **Author**: Xuka
- **GitHub**: [@Xuankhoa31789](https://github.com/Xuankhoa31789)
- **Repository**: [exam](https://github.com/Xuankhoa31789/exam)

---

<div align="center">

[View Issues](https://github.com/Xuankhoa31789/exam/issues) · [Suggest Features](https://github.com/Xuankhoa31789/exam/issues/new)

</div>
