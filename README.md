# 📚 Student Exam Management System

> A modern JavaFX application for student authentication and management.

![Version](https://img.shields.io/badge/version-1.0-blue.svg)
![Java](https://img.shields.io/badge/Java-23+-orange.svg)
![Status](https://img.shields.io/badge/status-early%20stage-yellow.svg)

## 🎯 Overview

The **Student Exam Management System** is a desktop application built with JavaFX that provides a user-friendly interface for student registration and authentication. This project is in early stages of development and focuses on core functionality with a clean, maintainable architecture.

### Current Features

✨ **Authentication System**
- Login interface with username/password validation
- Student registration form
- Navigation between screens

👤 **Student Management Foundation**
- Student data model with essential fields
- Data Access Object (DAO) pattern for future database integration
- Profile information structure

🎨 **Modern UI/UX**
- FXML-based responsive design
- Consistent styling across screens
- Scrollable forms for accessibility

## 📋 Table of Contents

- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Project Structure](#project-structure)
- [Architecture](#architecture)
- [Building & Running](#building--running)
- [Project Roadmap](#project-roadmap)

## 📦 Prerequisites

- **Java Development Kit (JDK)**: 23 or higher
- **Maven**: 3.6 or later
- **Git**: For version control (optional)

## 🚀 Quick Start

### Step 1: Clone the Repository

```bash
git clone https://github.com/Xuankhoa31789/exam.git
cd exam
```

### Step 2: Install Dependencies

```bash
mvn clean install
```

### Step 3: Run the Application

```bash
mvn javafx:run
```

The login screen will open. You can explore the registration flow and UI.

## 📁 Project Structure

```
exam/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/xuka/exam/
│   │   │       ├── ExamApplication.java        # Application entry point
│   │   │       ├── LoginController.java        # Login screen controller
│   │   │       ├── RegisterController.java     # Registration screen controller
│   │   │       ├── config/
│   │   │       │   └── HibernateUtil.java      # Hibernate configuration (future DB)
│   │   │       ├── dao/
│   │   │       │   └── StudentDAO.java         # Data access object pattern
│   │   │       └── models/
│   │   │           └── Student.java            # Student entity model
│   │   └── resources/
│   │       ├── com/xuka/exam/
│   │       │   ├── login_screen.fxml          # Login UI
│   │       │   └── register_screen.fxml       # Registration UI
│   │       └── META-INF/
│   │           └── persistence.xml             # JPA configuration
│   └── test/
│       └── java/
├── pom.xml                                     # Maven configuration
└── README.md                                   # This file
```

## 🏗️ Architecture

### MVC Pattern (Model-View-Controller)

The application follows the MVC design pattern for clean separation of concerns:

```
┌──────────────────────────────────┐
│    View Layer (FXML)             │
│  • login_screen.fxml             │
│  • register_screen.fxml          │
└─────────────┬──────────────────┘
              │
┌─────────────▼──────────────────┐
│  Controller Layer               │
│  • LoginController              │
│  • RegisterController           │
│  ├─ Input Validation            │
│  ├─ User Interactions           │
│  └─ Navigation Logic            │
└─────────────┬──────────────────┘
              │
┌─────────────▼──────────────────┐
│  Model Layer                    │
│  • Student (Entity)             │
│  ├─ fullName                    │
│  ├─ email                       │
│  ├─ phone                       │
│  ├─ dateOfBirth                 │
│  └─ credentials                 │
└─────────────┬──────────────────┘
              │
┌─────────────▼──────────────────┐
│  DAO Layer (Future)             │
│  • StudentDAO                   │
│  ├─ save()                      │
│  ├─ update()                    │
│  ├─ delete()                    │
│  └─ query()                     │
└──────────────────────────────────┘
```

### Core Components

| Component | Purpose |
|---|---|
| **Student.java** | Entity model representing student data structure |
| **StudentDAO.java** | Data Access Object - abstraction for data operations |
| **LoginController.java** | Handles login screen logic and authentication |
| **RegisterController.java** | Manages registration form and student creation |
| **ExamApplication.java** | Application entry point and stage initialization |
| **HibernateUtil.java** | Configuration utility for future database integration |

## 🛠️ Building & Running

### Build the Project

```bash
# Clean and build
mvn clean package

# Build with tests
mvn clean verify
```

### Run Application

```bash
# Development mode (recommended)
mvn javafx:run

# Build JAR and run
mvn clean package
java -m javafx.controls,javafx.fxml -cp target/exam-1.0-SNAPSHOT.jar com.xuka.exam.ExamApplication
```

## 📊 Technology Stack

| Technology | Version | Purpose |
|---|---|---|
| Java | 23+ | Programming language |
| JavaFX | 17.0.6 | GUI framework |
| Hibernate ORM | 7.3.1 | Future database integration |
| Maven | 3.6+ | Build automation |
| MySQL Connector | 9.6.0 | Future database driver |

## 🚧 Project Roadmap

**Phase 1 (Current)**: Early Foundation
- ✅ Login/Registration UI
- ✅ FXML form design
- ✅ Navigation between screens
- ✅ Basic form validation
- 🔄 Input form handling

**Phase 2 (Next)**: Database Integration
- ⏳ MySQL database setup
- ⏳ Hibernate ORM configuration
- ⏳ DAO implementation for persistence
- ⏳ Student record storage

**Phase 3 (Future)**: Enhanced Features
- ⏳ User session management
- ⏳ Dashboard/Profile screens
- ⏳ Advanced validation rules
- ⏳ Password hashing/security

**Phase 4 (Future)**: Admin & Reporting
- ⏳ Admin panel
- ⏳ Student management interface
- ⏳ Reports and analytics
- ⏳ Exam scheduling

## 🤝 Contributing

Contributions are welcome! To contribute:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/YourFeature`)
3. Commit your changes (`git commit -m 'Add YourFeature'`)
4. Push to the branch (`git push origin feature/YourFeature`)
5. Open a Pull Request

## 📝 Code Style

- Follow Java naming conventions
- Use descriptive variable names
- Add comments for complex logic
- Keep methods focused and concise
- Use FXML for UI layout

## 🐛 Known Issues & Limitations

- Database operations not yet implemented
- No user session persistence
- Form validation is basic
- Limited error handling

## 📧 Contact

- **Author**: Xuka
- **GitHub**: [@Xuankhoa31789](https://github.com/Xuankhoa31789)
- **Repository**: [exam](https://github.com/Xuankhoa31789/exam)

---

<div align="center">

**⭐ This project is in early stages - contributions welcome!**

[View Issues](https://github.com/Xuankhoa31789/exam/issues) · [Suggest Features](https://github.com/Xuankhoa31789/exam/issues)

</div>
