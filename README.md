# Course Management System

A modular, object-oriented JavaFX application for managing every aspect of an academic course lifecycle. Developed as part of the **Advanced Programming** course at **Helwan National University**, this system provides tailored interfaces for administrators, instructors, and students—and persists all data via Java serialization.

---

## 📌 Overview

Managing courses, enrollments, assignments, and grades can be complex. This application streamlines the process by offering:

- **Role-based Dashboards**
  - 👨‍💼 Admin: User management, course creation, grade oversight, and audit logging.
  - 👨‍🏫 Instructor: Assignment creation, grade entry, and class roster management.
  - 👨‍🎓 Student: Course enrollment, assignment submission, and grade review.

- **Persistent Data Storage**
  - All entities (users, courses, enrollments, assignments, grades) are stored using `.ser` files.

- **Intuitive JavaFX UI**
  - Built with FXML layouts for clean separation of design and logic, enabling rapid prototyping and easy styling.

---

## 📁 Project Structure

<pre>
CourseManagementSystem/
├── pom.xml                          # Maven build configuration
├── nbactions.xml                    # NetBeans custom actions
├── src/
│   ├── main/
│   │   ├── java/com/coursemanagement/
│   │   │   ├── AdminUser.java
│   │   │   ├── Assignment.java
│   │   │   ├── AuthenticationManager.java
│   │   │   ├── Course.java
│   │   │   ├── CourseGrade.java
│   │   │   ├── CourseManagementApp.java
│   │   │   ├── FileStorageManager.java
│   │   │   ├── LoggingUtil.java
│   │   │   ├── Student.java
│   │   │   └── ValidationUtil.java
│   │   └── resources/com/coursemanagement/
│   │       ├── primary.fxml
│   │       └── secondary.fxml
│   └── test/java/…                  # (optional) unit tests
├── users.ser
├── courses.ser
├── enrollments.ser
├── assignments.ser
├── grades.ser
└── instructor_assignments.ser
</pre>

---

## ⚙️ Key Features

### 1. Authentication & Authorization
- Secure login, logout, and role checking.
- `EnhancedAuthenticationManager` supports future multi-factor or OAuth extensions.

### 2. Data Persistence
- Generic `FileStorageManager` for reading/writing all `.ser` files.
- Version-controlled serialized files for reproducibility.

### 3. JavaFX Interface
- Clean FXML layout-based GUI.
- Dynamic dashboards based on user role.

### 4. Error Handling & Logging
- Centralized `LoggingUtil` for exception tracing and user activity.
- `ErrorHandlingUtil` provides consistent, user-friendly messages.

### 5. Extensibility
- Clearly separated packages for OOP maintainability.
- Easily extendable with new features (e.g., reports, schedules, messaging).

---

## 🚀 How to Run

### Requirements:
- Java 11+  
- Maven  
- NetBeans or IntelliJ IDEA (preferred)

### Steps:
```bash
git clone https://github.com/ali-ezz/course-management-system.git
cd course-management-system
mvn clean javafx:run
