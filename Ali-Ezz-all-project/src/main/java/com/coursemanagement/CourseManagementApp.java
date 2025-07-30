package com.coursemanagement;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDate;
import java.util.*;
import java.io.*;

public class CourseManagementApp extends Application {

    public static final String COURSE_DB_FILE = "./courses.ser";
    public static final String USER_DB_FILE = "./users.ser";
    public static final String ENROLLMENTS_DB_FILE = "./enrollments.ser";
    public static final String INSTRUCTOR_ASSIGNMENTS_DB_FILE = "./instructor_assignments.ser";
    public static final String ASSIGNMENTS_DB_FILE = "./assignments.ser";
    public static final String GRADES_DB_FILE = "./grades.ser";
    public static final String LOG_FILE = "./logs/course_management.log";

    public static final String COURSE_TXT_FILE = "./courses.txt";
    public static final String USER_TXT_FILE = "./users.txt";
    public static final String ENROLLMENTS_TXT_FILE = "./enrollments.txt";
    public static final String INSTRUCTOR_ASSIGNMENTS_TXT_FILE = "./instructor_assignments.txt";
    public static final String ASSIGNMENTS_TXT_FILE = "./assignments.txt";
    public static final String GRADES_TXT_FILE = "./grades.txt";

    private FileStorageManager<Course> courseStorageManager = new FileStorageManager<>();
    private FileStorageManager<Assignment> assignmentStorageManager = new FileStorageManager<>();
    private FileStorageManager<CourseGrade> gradesStorageManager = new FileStorageManager<>();

    private AuthenticationManager authManager;
    private Stage primaryStage;

    private ObservableList<Course> courseData = FXCollections.observableArrayList();
    private ObservableList<Assignment> allAssignments = FXCollections.observableArrayList();
    private Map<String, Set<String>> enrollmentMap = new HashMap<>();
    private Map<String, String> instructorAssignments = new HashMap<>();
    private Map<String, List<Assignment>> assignmentsPerCourse = new HashMap<>();
    private Map<String, Map<String, CourseGrade>> gradesPerStudent = new HashMap<>();

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        authManager = new AuthenticationManager();
        loadCoursesFromFile();
        loadEnrollmentsFromFile();
        loadInstructorAssignmentsFromFile();
        loadAssignmentsFromFile();
        loadGradesFromFile();
        LoggingUtil.logInfo("System started. " + fileInfo());
        primaryStage.setTitle("Course Management System");
        Scene loginScene = createLoginScene();
        primaryStage.setScene(loginScene);
        primaryStage.setWidth(420);
        primaryStage.setHeight(340);
        primaryStage.show();
    }

    private String fileInfo() {
        return String.format("Files used: courses=%s, users=%s, logs=%s",
            new File(COURSE_DB_FILE).getAbsolutePath(),
            new File(USER_DB_FILE).getAbsolutePath(),
            new File(LOG_FILE).getAbsolutePath());
    }

    private void showFileInfoDialog() {
        String info = "File Locations:\n";
        info += "Courses: " + new File(COURSE_DB_FILE).getAbsolutePath() + "\n";
        info += "Users: " + new File(USER_DB_FILE).getAbsolutePath() + "\n";
        info += "Log File: " + new File(LOG_FILE).getAbsolutePath() + "\n";
        showInfoDialog("File Locations", null, info);
    }

    private void loadCoursesFromFile() {
        try {
            List<Course> loaded = courseStorageManager.loadFromFile(COURSE_DB_FILE);
            courseData.setAll(loaded != null ? loaded : new ArrayList<>());
            saveCoursesToText();
            LoggingUtil.logInfo("Courses loaded from file: " + courseData.size());
        } catch (Exception ex) {
            LoggingUtil.logError("Failed to load courses", ex);
            showErrorDialog("File Load Error", "Could not load courses from file.");
        }
    }

    private void saveCoursesToFile() {
        try {
            courseStorageManager.saveToFile(new ArrayList<>(courseData), COURSE_DB_FILE);
            saveCoursesToText();
            LoggingUtil.logInfo("Courses saved to file. Total: " + courseData.size());
        } catch (Exception ex) {
            LoggingUtil.logError("Courses file save error", ex);
            showErrorDialog("File Save Error", "Could not save courses to file.");
        }
    }
    private void saveCoursesToText() {
        try (PrintWriter out = new PrintWriter(new FileWriter(COURSE_TXT_FILE))) {
            for (Course c : courseData)
                out.println(c.getCourseCode() + "\t" + c.getCourseName() + "\t" + c.getInstructorName() + "\t" + c.getCredits());
        } catch (IOException ignored) {}
    }

    private ObservableList<User> loadUsersFromFile() {
        try {
            List<User> users = new ArrayList<>(authManager.userDatabase.values());
            saveUsersToText(users);
            LoggingUtil.logInfo("Users loaded: " + users.size());
            return FXCollections.observableArrayList(users);
        } catch (Exception ex) {
            LoggingUtil.logError("Users file load error", ex);
            showErrorDialog("User Load Error", "Could not load users from file.");
            return FXCollections.observableArrayList();
        }
    }

    private void saveUsersToFile(List<User> users) {
        try {
            Map<String, User> map = new HashMap<>();
            for (User u : users) map.put(u.getEmail(), u);
            authManager.userDatabase = map;
            java.lang.reflect.Method method = AuthenticationManager.class.getDeclaredMethod("saveUsers");
            method.setAccessible(true);
            method.invoke(authManager);
            saveUsersToText(users);
            LoggingUtil.logInfo("Users saved to file. Total: " + users.size());
        } catch (Exception ex) {
            LoggingUtil.logError("Users file save error", ex);
            showErrorDialog("Users Save Error", "Could not save users to file.");
        }
    }

    private void saveUsersToText(List<User> users) {
        try (PrintWriter out = new PrintWriter(new FileWriter(USER_TXT_FILE))) {
            for (User u : users)
                out.println(u.getUserType() + "\t" + u.getName() + "\t" + u.getEmail() + "\t" + u.getUserId());
        } catch (IOException ignored) {}
    }

    private void loadEnrollmentsFromFile() {
        try {
            File file = new File(ENROLLMENTS_DB_FILE);
            if (file.exists()) {
                ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
                enrollmentMap = (Map<String, Set<String>>) in.readObject();
                in.close();
                saveEnrollmentsToText();
            }
        } catch (Exception ex) {
            enrollmentMap = new HashMap<>();
        }
    }
    private void saveEnrollmentsToFile() {
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(ENROLLMENTS_DB_FILE));
            out.writeObject(enrollmentMap);
            out.close();
            saveEnrollmentsToText();
        } catch (Exception ex) {}
    }
    private void saveEnrollmentsToText() {
        try (PrintWriter out = new PrintWriter(new FileWriter(ENROLLMENTS_TXT_FILE))) {
            for (Map.Entry<String, Set<String>> entry : enrollmentMap.entrySet()) {
                out.print(entry.getKey() + ":");
                for (String cid : entry.getValue()) out.print("\t" + cid);
                out.println();
            }
        } catch (IOException ignored) {}
    }

    private void loadInstructorAssignmentsFromFile() {
        try {
            File file = new File(INSTRUCTOR_ASSIGNMENTS_DB_FILE);
            if (file.exists()) {
                ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
                instructorAssignments = (Map<String, String>) in.readObject();
                in.close();
                saveInstructorAssignmentsToText();
            }
        } catch (Exception ex) {
            instructorAssignments = new HashMap<>();
        }
    }
    private void saveInstructorAssignmentsToFile() {
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(INSTRUCTOR_ASSIGNMENTS_DB_FILE));
            out.writeObject(instructorAssignments);
            out.close();
            saveInstructorAssignmentsToText();
        } catch (Exception ex) {}
    }
    private void saveInstructorAssignmentsToText() {
        try (PrintWriter out = new PrintWriter(new FileWriter(INSTRUCTOR_ASSIGNMENTS_TXT_FILE))) {
            for (Map.Entry<String, String> entry : instructorAssignments.entrySet())
                out.println(entry.getKey() + "\t" + entry.getValue());
        } catch (IOException ignored) {}
    }

    private void loadAssignmentsFromFile() {
        try {
            File file = new File(ASSIGNMENTS_DB_FILE);
            if (file.exists()) {
                ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
                allAssignments = FXCollections.observableArrayList((List<Assignment>) in.readObject());
                assignmentsPerCourse.clear();
                for (Assignment a : allAssignments) {
                    assignmentsPerCourse.computeIfAbsent(a.getCourseCode(), k -> new ArrayList<>()).add(a);
                }
                in.close();
                saveAssignmentsToText();
            }
        } catch (Exception ex) {
            allAssignments = FXCollections.observableArrayList();
        }
    }
    private void saveAssignmentsToFile() {
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(ASSIGNMENTS_DB_FILE));
            out.writeObject(new ArrayList<>(allAssignments));
            out.close();
            saveAssignmentsToText();
        } catch (Exception ex) {}
    }
    private void saveAssignmentsToText() {
        try (PrintWriter out = new PrintWriter(new FileWriter(ASSIGNMENTS_TXT_FILE))) {
            for (Assignment a : allAssignments)
                out.println(a.getCourseCode() + "\t" + a.getTitle() + "\t" + a.getDueDate() + "\t" + a.getStatus());
        } catch (IOException ignored) {}
    }

    private void loadGradesFromFile() {
        try {
            File file = new File(GRADES_DB_FILE);
            if (file.exists()) {
                ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
                gradesPerStudent = (Map<String, Map<String, CourseGrade>>) in.readObject();
                in.close();
                saveGradesToText();
            }
        } catch (Exception ex) {
            gradesPerStudent = new HashMap<>();
        }
    }
    private void saveGradesToFile() {
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(GRADES_DB_FILE));
            out.writeObject(gradesPerStudent);
            out.close();
            saveGradesToText();
        } catch (Exception ex) {}
    }
    private void saveGradesToText() {
        try (PrintWriter out = new PrintWriter(new FileWriter(GRADES_TXT_FILE))) {
            for (Map.Entry<String, Map<String, CourseGrade>> entry : gradesPerStudent.entrySet()) {
                String stuId = entry.getKey();
                for (Map.Entry<String, CourseGrade> courseGrade : entry.getValue().entrySet()) {
                    out.println(stuId + "\t" + courseGrade.getKey() + "\t" +
                            courseGrade.getValue().getGrade() + "\t" +
                            courseGrade.getValue().getPercentage());
                }
            }
        } catch (IOException ignored) {}
    }




public static class GradeUtil {
    private static final Map<String, Double> LETTER_TO_PERCENTAGE = new HashMap<>();
    private static final Map<String, Double> LETTER_TO_GPA = new HashMap<>();
    static {
        LETTER_TO_PERCENTAGE.put("A+", 98.0); LETTER_TO_GPA.put("A+", 4.0);
        LETTER_TO_PERCENTAGE.put("A", 95.0);  LETTER_TO_GPA.put("A", 4.0);
        LETTER_TO_PERCENTAGE.put("A-", 90.0); LETTER_TO_GPA.put("A-", 3.7);
        LETTER_TO_PERCENTAGE.put("B+", 87.0); LETTER_TO_GPA.put("B+", 3.3);
        LETTER_TO_PERCENTAGE.put("B", 84.0);  LETTER_TO_GPA.put("B", 3.0);
        LETTER_TO_PERCENTAGE.put("B-", 80.0); LETTER_TO_GPA.put("B-", 2.7);
        LETTER_TO_PERCENTAGE.put("C+", 77.0); LETTER_TO_GPA.put("C+", 2.3);
        LETTER_TO_PERCENTAGE.put("C", 74.0);  LETTER_TO_GPA.put("C", 2.0);
        LETTER_TO_PERCENTAGE.put("C-", 70.0); LETTER_TO_GPA.put("C-", 1.7);
        LETTER_TO_PERCENTAGE.put("D+", 67.0); LETTER_TO_GPA.put("D+", 1.3);
        LETTER_TO_PERCENTAGE.put("D", 64.0);  LETTER_TO_GPA.put("D", 1.0);
        LETTER_TO_PERCENTAGE.put("D-", 60.0); LETTER_TO_GPA.put("D-", 0.7);
        LETTER_TO_PERCENTAGE.put("F",  50.0); LETTER_TO_GPA.put("F", 0.0);
    }
    public static double toPercentage(String letter) {
        return LETTER_TO_PERCENTAGE.getOrDefault(letter.trim().toUpperCase(), 0.0);
    }
    public static double toGpa(String letter) {
        return LETTER_TO_GPA.getOrDefault(letter.trim().toUpperCase(), 0.0);
    }
}


private boolean isNumeric(String s) {
    if (s == null) return false;
    try {
        Double.parseDouble(s);
        return true;
    } catch (NumberFormatException ex) {
        return false;
    }
}

private String percentToLetter(double percent) {
    if (percent >= 97) return "A+";
    if (percent >= 93) return "A";
    if (percent >= 90) return "A-";
    if (percent >= 87) return "B+";
    if (percent >= 83) return "B";
    if (percent >= 80) return "B-";
    if (percent >= 77) return "C+";
    if (percent >= 73) return "C";
    if (percent >= 70) return "C-";
    if (percent >= 67) return "D+";
    if (percent >= 63) return "D";
    if (percent >= 60) return "D-";
    return "F";
}
    private String mainBackground = "-fx-background-color: linear-gradient(to bottom,#fff,#e6e6fa 90%);";
    private String dashboardPanel = "-fx-background-radius: 12; -fx-background-insets: 0 1 1 1; -fx-background-color: #ffffffee; "
                                + "-fx-effect: dropshadow(gaussian,rgba(100,100,200,0.13),24,0.5,0.5,0.5);"
                                + "-fx-border-color: #b3d4fc;-fx-border-radius: 10;";
    private String buttonStyle = "-fx-background-color: #6c63ff; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;";
    private String buttonDanger = "-fx-background-color: #ff5370; -fx-text-fill: white; -fx-background-radius: 8;";
    private String buttonSubtle = "-fx-background-color: #f7f7fa; -fx-text-fill: #6c63ff; -fx-border-color: #6c63ff; -fx-background-radius: 8;";
    private String heading1 = "-fx-font-size: 19px; -fx-font-weight: 800;-fx-text-fill:#5e6278;";
    private String panelPadding = "14";

    private Scene createLoginScene() {
        VBox loginLayout = new VBox(18);
        loginLayout.setPadding(new Insets(36, 32, 36, 32));
        loginLayout.setAlignment(Pos.CENTER);
        loginLayout.setStyle(mainBackground + dashboardPanel);

        Label titleLabel = new Label("Course Management Login");
        titleLabel.setStyle(heading1);

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setStyle("-fx-background-radius: 8;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setStyle("-fx-background-radius: 8;");

        Button loginButton = new Button("Login");
        loginButton.setStyle(buttonStyle);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #d63c3c; -fx-font-size: 13px;");

        loginButton.setOnAction(e -> {
            boolean loginSuccessful = authManager.authenticate(
                emailField.getText(),
                passwordField.getText()
            );
            if (loginSuccessful) {
                LoggingUtil.logInfo("Login success: "+emailField.getText());
                openDashboardBasedOnUserRole();
            } else {
                errorLabel.setText("Invalid email or password");
                LoggingUtil.logWarning("Login failed: "+emailField.getText());
            }
        });

        Button fileInfoBtn = new Button("Show Data File Locations");
        fileInfoBtn.setStyle(buttonSubtle);
        fileInfoBtn.setOnAction(e -> showFileInfoDialog());

        HBox btnRow = new HBox(14, loginButton, fileInfoBtn);
        btnRow.setAlignment(Pos.CENTER);

        loginLayout.getChildren().addAll(titleLabel, emailField, passwordField, btnRow, errorLabel);

        return new Scene(loginLayout, 380, 320);
    }

    private void openDashboardBasedOnUserRole() {
        User currentUser = authManager.getCurrentUser();
        if (currentUser instanceof AdminUser) openAdminDashboard((AdminUser) currentUser);
        else if (currentUser instanceof InstructorUser) openInstructorDashboard((InstructorUser) currentUser);
        else if (currentUser instanceof Student) openStudentDashboard((Student) currentUser);
    }

    private void openAdminDashboard(AdminUser admin) {
        VBox layout = new VBox(16);
        layout.setPadding(new Insets(Integer.parseInt(panelPadding)));
        layout.setSpacing(18);
        layout.setStyle(mainBackground + dashboardPanel);
        layout.setAlignment(Pos.CENTER);

        Label welcomeLabel = new Label("Admin Dashboard - " + admin.getName());
        welcomeLabel.setStyle(heading1);

        Button manageCourses = new Button("Manage Courses");
        Button manageUsers = new Button("Manage Users");
        Button generateReports = new Button("Generate Reports");
        Button assignInstructors = new Button("Assign Instructors to Courses");
        Button fileInfoBtn = new Button("File Locations");
        Button logoutButton = new Button("Logout");

        List<Button> adminBtns = Arrays.asList(manageCourses, manageUsers, generateReports, assignInstructors, fileInfoBtn, logoutButton);
        for (Button b : adminBtns) b.setStyle(buttonStyle);
        logoutButton.setStyle(buttonDanger);

        manageCourses.setOnAction(e -> showManageCoursesDialog());
        manageUsers.setOnAction(e -> showManageUsersDialog());
        generateReports.setOnAction(e -> showReportsDialog());
        assignInstructors.setOnAction(e -> showAssignInstructorsDialog());
        fileInfoBtn.setOnAction(e -> showFileInfoDialog());
        logoutButton.setOnAction(e -> {
            LoggingUtil.logInfo("Admin logged out.");
            authManager.logout();
            primaryStage.setScene(createLoginScene());
        });

        layout.getChildren().addAll(welcomeLabel, manageCourses, manageUsers, generateReports, assignInstructors, fileInfoBtn, logoutButton);
        primaryStage.setScene(new Scene(layout, 420, 540));
    }

    private void openInstructorDashboard(InstructorUser instructor) {
        VBox layout = new VBox(16);
        layout.setPadding(new Insets(Integer.parseInt(panelPadding)));
        layout.setSpacing(18);
        layout.setStyle(mainBackground + dashboardPanel);
        layout.setAlignment(Pos.CENTER);

        Label welcomeLabel = new Label("Instructor Dashboard - " + instructor.getName());
        welcomeLabel.setStyle(heading1);

        Button manageCourseContent = new Button("Manage Course Content");
        Button gradeStudents = new Button("Grade Students");
        Button viewEnrollments = new Button("View Course Enrollments");
        Button createAssignments = new Button("Create Assignments");
        Button fileInfoBtn = new Button("File Locations");
        Button logoutButton = new Button("Logout");

        List<Button> instrBtns = Arrays.asList(manageCourseContent, gradeStudents, viewEnrollments, createAssignments, fileInfoBtn, logoutButton);
        for (Button b : instrBtns) b.setStyle(buttonStyle);
        logoutButton.setStyle(buttonDanger);

        manageCourseContent.setOnAction(e -> showManageCourseContentDialog(instructor));
        gradeStudents.setOnAction(e -> showGradeStudentsDialog(instructor));
        viewEnrollments.setOnAction(e -> showCourseEnrollmentViewDialog(instructor));
        createAssignments.setOnAction(e -> showAssignmentsDialog(instructor));
        fileInfoBtn.setOnAction(e -> showFileInfoDialog());
        logoutButton.setOnAction(e -> {
            LoggingUtil.logInfo("Instructor logged out.");
            authManager.logout();
            primaryStage.setScene(createLoginScene());
        });

        layout.getChildren().addAll(welcomeLabel, manageCourseContent, gradeStudents, viewEnrollments, createAssignments, fileInfoBtn, logoutButton);
        primaryStage.setScene(new Scene(layout, 420, 540));
    }

    private void openStudentDashboard(Student student) {
        VBox layout = new VBox(16);
        layout.setPadding(new Insets(Integer.parseInt(panelPadding)));
        layout.setSpacing(18);
        layout.setStyle(mainBackground + dashboardPanel);
        layout.setAlignment(Pos.CENTER);

        Label welcomeLabel = new Label("Student Dashboard - " + student.getName());
        welcomeLabel.setStyle(heading1);

        Button viewCourses = new Button("View Available Courses");
        Button enrollCourses = new Button("Enroll in Courses");
        Button viewGrades = new Button("View Grades");
        Button viewAssignments = new Button("View Assignments");
        Button fileInfoBtn = new Button("File Locations");
        Button logoutButton = new Button("Logout");

        List<Button> studentBtns = Arrays.asList(viewCourses, enrollCourses, viewGrades, viewAssignments, fileInfoBtn, logoutButton);
        for (Button b : studentBtns) b.setStyle(buttonStyle);
        logoutButton.setStyle(buttonDanger);

        viewCourses.setOnAction(e -> showAvailableCoursesDialog());
        enrollCourses.setOnAction(e -> showCourseEnrollmentDialog());
        viewGrades.setOnAction(e -> showGradesDialog());
        viewAssignments.setOnAction(e -> showStudentAssignmentsDialog(student));
        fileInfoBtn.setOnAction(e -> showFileInfoDialog());
        logoutButton.setOnAction(e -> {
            LoggingUtil.logInfo("Student logged out.");
            authManager.logout();
            primaryStage.setScene(createLoginScene());
        });

        layout.getChildren().addAll(welcomeLabel, viewCourses, enrollCourses, viewGrades, viewAssignments, fileInfoBtn, logoutButton);
        primaryStage.setScene(new Scene(layout, 420, 540));
    }
    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfoDialog(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    private void showManageUsersDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Manage Users");
        dialog.setHeaderText("User Management");

        TableView<User> usersTable = new TableView<>();
        usersTable.setMinHeight(200);

        TableColumn<User, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<User, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(param ->
            new javafx.beans.property.SimpleStringProperty(param.getValue().getUserType()));
        TableColumn<User, String> mailCol = new TableColumn<>("Email");
        mailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        usersTable.getColumns().addAll(nameCol, typeCol, mailCol);

        ObservableList<User> userItems = loadUsersFromFile();
        usersTable.setItems(userItems);

        Button addBtn = new Button("Add User");
        Button editBtn = new Button("Edit User");
        Button delBtn = new Button("Delete User");

        addBtn.setOnAction(e -> {
            User user = showUserEditDialog(null);
            if (user != null) {
                userItems.add(user);
                saveUsersToFile(userItems);
                usersTable.refresh();
                LoggingUtil.logInfo("User added: " + user.getEmail());
            }
        });
        editBtn.setOnAction(e -> {
            User sel = usersTable.getSelectionModel().getSelectedItem();
            if (sel != null) {
                User upd = showUserEditDialog(sel);
                if (upd != null) {
                    int idx = userItems.indexOf(sel);
                    userItems.set(idx, upd);
                    saveUsersToFile(userItems);
                    usersTable.refresh();
                    LoggingUtil.logInfo("User updated: " + upd.getEmail());
                }
            } else {
                showInfoDialog("Edit Error", "No user selected", "Select a user first!");
            }
        });
        delBtn.setOnAction(e -> {
            User sel = usersTable.getSelectionModel().getSelectedItem();
            if (sel != null) {
                userItems.remove(sel);
                saveUsersToFile(userItems);
                usersTable.refresh();
                LoggingUtil.logInfo("User deleted: " + sel.getEmail());
            }
        });

        HBox buttonRow = new HBox(10, addBtn, editBtn, delBtn);
        VBox panel = new VBox(10, usersTable, buttonRow);
        dialog.getDialogPane().setContent(panel);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
    private User showUserEditDialog(User u) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(u == null ? "Add User" : "Edit User");
        dialog.setHeaderText(u == null ? "Create new user" : "Edit user");
        ComboBox<String> typeBox = new ComboBox<>(FXCollections.observableArrayList("ADMIN", "INSTRUCTOR", "STUDENT"));
        TextField nameField = new TextField(u == null ? "" : u.getName());
        TextField emailField = new TextField(u == null ? "" : u.getEmail());
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Leave blank to keep current password.");
        if (u == null) typeBox.setValue("STUDENT");
        else typeBox.setValue(u.getUserType());
        VBox box = new VBox(10,
            new Label("Type:"), typeBox,
            new Label("Name:"), nameField,
            new Label("Email:"), emailField,
            new Label("Password:"), passwordField
        );
        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String name = nameField.getText().trim(), email = emailField.getText().trim();
            String passwd = passwordField.getText().trim();
            if (name.isEmpty() || email.isEmpty() || (u == null && passwd.isEmpty())) {
                showInfoDialog("Input Error", "Invalid input", "All fields except password (when editing) are required.");
                return null;
            }
            String type = typeBox.getValue();
            String userId = (u == null) ? UUID.randomUUID().toString() : u.getUserId();
            String finalPassword = (passwd.isEmpty() && u != null) ? u.getPassword() : passwd;
            switch (type) {
                case "ADMIN":
                    return new AdminUser(userId, name, email, finalPassword);
                case "INSTRUCTOR":
                    return new InstructorUser(userId, name, email, finalPassword);
                default:
                case "STUDENT":
                    return new Student(userId, name, email, finalPassword);
            }
        }
        return null;
    }
    private void showManageCoursesDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Manage Courses");
        dialog.setHeaderText("Course Management");

        TableView<Course> coursesTable = new TableView<>();
        coursesTable.setMinHeight(200);

        TableColumn<Course, String> codeCol = new TableColumn<>("Course Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        TableColumn<Course, String> nameCol = new TableColumn<>("Course Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        TableColumn<Course, String> instrCol = new TableColumn<>("Instructor");
        instrCol.setCellValueFactory(new PropertyValueFactory<>("instructorName"));
        TableColumn<Course, Integer> creditsCol = new TableColumn<>("Credits");
        creditsCol.setCellValueFactory(new PropertyValueFactory<>("credits"));
        coursesTable.getColumns().addAll(codeCol, nameCol, instrCol, creditsCol);

        coursesTable.setItems(courseData);

        Button addBtn = new Button("Add Course");
        Button editBtn = new Button("Edit Course");
        Button delBtn = new Button("Delete Course");

        addBtn.setOnAction(e -> {
            Course newC = showCourseEditDialog(null);
            if (newC != null) {
                courseData.add(newC);
                saveCoursesToFile();
                LoggingUtil.logInfo("Course added: " + newC.getCourseCode());
            }
        });
        editBtn.setOnAction(e -> {
            Course sel = coursesTable.getSelectionModel().getSelectedItem();
            if (sel != null) {
                Course upd = showCourseEditDialog(sel);
                if (upd != null) {
                    int idx = courseData.indexOf(sel);
                    courseData.set(idx, upd);
                    saveCoursesToFile();
                    LoggingUtil.logInfo("Course updated: " + upd.getCourseCode());
                }
            } else {
                showInfoDialog("Edit Error", "No course selected", "Select a course to edit.");
            }
        });
        delBtn.setOnAction(e -> {
            Course sel = coursesTable.getSelectionModel().getSelectedItem();
            if (sel != null) {
                courseData.remove(sel);
                saveCoursesToFile();
                LoggingUtil.logInfo("Course deleted: " + sel.getCourseCode());
            }
        });

        HBox btnRow = new HBox(10, addBtn, editBtn, delBtn);
        VBox layout = new VBox(10, coursesTable, btnRow);

        dialog.getDialogPane().setContent(layout);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
    private Course showCourseEditDialog(Course course) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(course == null ? "Add Course" : "Edit Course");
        dialog.setHeaderText("Enter Course Details");

        TextField codeField = new TextField(course == null ? "" : course.getCourseCode());
        TextField nameField = new TextField(course == null ? "" : course.getCourseName());
        TextField instrField = new TextField(course == null ? "" : course.getInstructorName());
        TextField creditsField = new TextField(course == null ? "" : Integer.toString(course.getCredits()));
        VBox box = new VBox(10,
            new Label("Course Code:"), codeField,
            new Label("Course Name:"), nameField,
            new Label("Instructor(s):"), instrField,
            new Label("Credits:"), creditsField
        );
        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                int credits = Integer.parseInt(creditsField.getText().trim());
                return new Course(
                    codeField.getText().trim(),
                    nameField.getText().trim(),
                    instrField.getText().trim(),
                    credits
                );
            } catch (Exception ex) {
                showInfoDialog("Input Error", "Invalid Input", "Please check all fields.");
            }
        }
        return null;
    }
    private void showAssignInstructorsDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Assign Instructors to Courses");
        dialog.setHeaderText("Assign an instructor for each course");

        TableView<Course> courseTable = new TableView<>(courseData);
        courseTable.setPrefHeight(220);
        TableColumn<Course, String> codeCol = new TableColumn<>("Course Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        TableColumn<Course, String> nameCol = new TableColumn<>("Course Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        TableColumn<Course, String> instrCol = new TableColumn<>("Instructor");
        instrCol.setCellValueFactory(cellData -> {
            String cid = cellData.getValue().getCourseCode();
            String instrId = instructorAssignments.getOrDefault(cid, cellData.getValue().getInstructorName());
            if (instrId == null) instrId = "";
            return new javafx.beans.property.SimpleStringProperty(getUserNameFromId(instrId));
        });
        courseTable.getColumns().addAll(codeCol, nameCol, instrCol);

        ObservableList<User> instructorList = loadUsersFromFile().filtered(u -> u instanceof InstructorUser);

        ComboBox<User> instrCombo = new ComboBox<>(instructorList);
        instrCombo.setPromptText("Choose instructor");

        Button assignBtn = new Button("Assign to Selected Course");

        assignBtn.setOnAction(e -> {
            Course selCourse = courseTable.getSelectionModel().getSelectedItem();
            User selInstr = instrCombo.getSelectionModel().getSelectedItem();
            if (selCourse != null && selInstr != null) {
                instructorAssignments.put(selCourse.getCourseCode(), selInstr.getUserId());
                saveInstructorAssignmentsToFile();
                showInfoDialog("Instructor assigned", null, selInstr.getName() + " assigned to " + selCourse.getCourseName());
                courseTable.refresh();
            }
        });

        VBox layout = new VBox(12, new HBox(8, courseTable, instrCombo), assignBtn);
        dialog.getDialogPane().setContent(layout);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
    private String getUserNameFromId(String id) {
        if (id == null) return "";
        ObservableList<User> users = loadUsersFromFile();
        for (User u : users) if (id.equals(u.getUserId())) return u.getName();
        return "";
    }
    private void showCourseEnrollmentDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Course Enrollment");
        dialog.setHeaderText("Available Courses for Enrollment");

        TableView<Course> courseTable = new TableView<>(courseData);
        TableColumn<Course, String> codeCol = new TableColumn<>("Course Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        TableColumn<Course, String> nameCol = new TableColumn<>("Course Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        TableColumn<Course, String> instrCol = new TableColumn<>("Instructor");
        instrCol.setCellValueFactory(new PropertyValueFactory<>("instructorName"));
        TableColumn<Course, Integer> creditsCol = new TableColumn<>("Credits");
        creditsCol.setCellValueFactory(new PropertyValueFactory<>("credits"));
        courseTable.getColumns().addAll(codeCol, nameCol, instrCol, creditsCol);

        Button enrollButton = new Button("Enroll in Selected Course");

        enrollButton.setOnAction(e -> {
            Course selectedCourse = courseTable.getSelectionModel().getSelectedItem();
            if (selectedCourse != null) {
                User student = authManager.getCurrentUser();
                if (student instanceof Student) {
                    String sid = student.getUserId();
                    enrollmentMap.computeIfAbsent(sid, k -> new HashSet<>()).add(selectedCourse.getCourseCode());
                    saveEnrollmentsToFile();
                    showInfoDialog("Enrollment Successful", null, "You have been enrolled in " + selectedCourse.getCourseName());
                }
            }
        });

        VBox layout = new VBox(10, courseTable, enrollButton);
        dialog.getDialogPane().setContent(layout);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
    private void showCourseEnrollmentViewDialog(InstructorUser instr) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Course Enrollments");
        dialog.setHeaderText("Enrollments for your courses");

        List<Course> myCourses = new ArrayList<>();
        for (Course c : courseData) {
            if (instructorAssignments.getOrDefault(c.getCourseCode(), "").equals(instr.getUserId())) {
                myCourses.add(c);
            }
        }

        TableView<EnrollmentTableRow> table = new TableView<>();
        TableColumn<EnrollmentTableRow, String> courseCol = new TableColumn<>("Course");
        courseCol.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        TableColumn<EnrollmentTableRow, String> studentCol = new TableColumn<>("Student");
        studentCol.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        table.getColumns().addAll(courseCol, studentCol);

        ObservableList<EnrollmentTableRow> rows = FXCollections.observableArrayList();
        for (Course c : myCourses) {
            for (Map.Entry<String, Set<String>> entry : enrollmentMap.entrySet()) {
                if (entry.getValue().contains(c.getCourseCode())) {
                    String sid = entry.getKey();
                    User stu = findUserById(sid);
                    if (stu != null) {
                        rows.add(new EnrollmentTableRow(c.getCourseName(), stu.getName()));
                    }
                }
            }
        }
        table.setItems(rows);
        dialog.getDialogPane().setContent(new VBox(table));
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
    public static class EnrollmentTableRow {
        private String courseName, studentName;
        public EnrollmentTableRow(String course, String stu) { this.courseName = course; this.studentName = stu;}
        public String getCourseName() { return courseName; }
        public String getStudentName() { return studentName; }
    }
    private User findUserById(String id) {
        for (User u : loadUsersFromFile())
            if (u.getUserId().equals(id)) return u;
        return null;
    }
    private void showGradeStudentsDialog(InstructorUser instructor) {
    Dialog<ButtonType> dialog = new Dialog<>();
    dialog.setTitle("Grade Students");
    dialog.setHeaderText("Grade students in your courses");
    List<Course> myCourses = new ArrayList<>();
    for (Course c : courseData) {
        if (instructorAssignments.getOrDefault(c.getCourseCode(), "").equals(instructor.getUserId()))
            myCourses.add(c);
    }
    ComboBox<Course> courseCombo = new ComboBox<>(FXCollections.observableArrayList(myCourses));
    TableView<User> studentsTable = new TableView<>();
    TableColumn<User, String> nameCol = new TableColumn<>("Name");
    nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
    studentsTable.getColumns().add(nameCol);

    courseCombo.setOnAction(e -> {
        Course sel = courseCombo.getSelectionModel().getSelectedItem();
        ObservableList<User> students = FXCollections.observableArrayList();
        for (Map.Entry<String, Set<String>> entry : enrollmentMap.entrySet()) {
            if (entry.getValue().contains(sel.getCourseCode())) {
                User stu = findUserById(entry.getKey());
                if (stu != null) students.add(stu);
            }
        }
        studentsTable.setItems(students);
    });

    Button assignGradeBtn = new Button("Assign Grade");
    assignGradeBtn.setOnAction(e -> {
        Course selCourse = courseCombo.getSelectionModel().getSelectedItem();
        User selStu = studentsTable.getSelectionModel().getSelectedItem();
        if (selCourse != null && selStu != null) {
            TextInputDialog gradeDialog = new TextInputDialog();
            gradeDialog.setTitle("Assign Grade");
            gradeDialog.setHeaderText("Enter grade for " + selStu.getName() + " in " + selCourse.getCourseName());
            gradeDialog.setContentText("Grade (A/B+/B, or number 0~100):");
            Optional<String> result = gradeDialog.showAndWait();
            if (result.isPresent()) {
                String gradeStr = result.get().trim();
                double percent;
                String canonicalLetterGrade;
                if (isNumeric(gradeStr)) {
                    percent = Math.max(0.0, Math.min(100.0, Double.parseDouble(gradeStr)));
                    canonicalLetterGrade = percentToLetter(percent);
                } else {
                    canonicalLetterGrade = gradeStr.toUpperCase();
                    percent = GradeUtil.toPercentage(canonicalLetterGrade);
                }
                Map<String, CourseGrade> stuGrades = gradesPerStudent.computeIfAbsent(selStu.getUserId(), k -> new HashMap<>());
                CourseGrade cg = new CourseGrade(selCourse.getCourseName(), canonicalLetterGrade, percent);
                stuGrades.put(selCourse.getCourseCode(), cg);
                saveGradesToFile();
                showInfoDialog("Grade Assigned", null, "Assigned " + canonicalLetterGrade + " (" + percent + "%) for " + selStu.getName());
            }
        }
    });
    VBox layout = new VBox(10, new Label("Select Course:"), courseCombo, studentsTable, assignGradeBtn);
    dialog.getDialogPane().setContent(layout);
    dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
    dialog.showAndWait();
}
    private void showAssignmentsDialog(InstructorUser instr) {
    Dialog<ButtonType> dialog = new Dialog<>();
    dialog.setTitle("Assignments");
    dialog.setHeaderText("Create, Edit, or Delete Assignments for Your Courses");

    List<Course> myCourses = new ArrayList<>();
    for (Course c : courseData) {
        if (instructorAssignments.getOrDefault(c.getCourseCode(), "").equals(instr.getUserId())) {
            myCourses.add(c);
        }
    }

    ComboBox<Course> courseCombo = new ComboBox<>(FXCollections.observableArrayList(myCourses));
    TableView<Assignment> assignmentsTable = new TableView<>();
    TableColumn<Assignment, String> titleCol = new TableColumn<>("Title");
    titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
    TableColumn<Assignment, LocalDate> dueCol = new TableColumn<>("Due Date");
    dueCol.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
    TableColumn<Assignment, String> statusCol = new TableColumn<>("Status");
    statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

    assignmentsTable.getColumns().addAll(titleCol, dueCol, statusCol);

    courseCombo.setOnAction(e -> {
        Course sel = courseCombo.getSelectionModel().getSelectedItem();
        List<Assignment> assigns = assignmentsPerCourse.getOrDefault(sel.getCourseCode(), new ArrayList<>());
        assignmentsTable.setItems(FXCollections.observableArrayList(assigns));
    });

    Button addAssignmentBtn = new Button("Add Assignment");
    addAssignmentBtn.setOnAction(e -> {
        Course sel = courseCombo.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        Dialog<ButtonType> ad = new Dialog<>();
        ad.setTitle("Add Assignment");
        TextField titleField = new TextField();
        DatePicker duePicker = new DatePicker(LocalDate.now().plusDays(7));
        ComboBox<String> statusBox = new ComboBox<>(FXCollections.observableArrayList("Pending", "Completed", "Overdue"));
        statusBox.setValue("Pending");
        VBox vb = new VBox(new Label("Assignment Title"), titleField, new Label("Due Date"), duePicker, new Label("Status"), statusBox);
        ad.getDialogPane().setContent(vb);
        ad.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Optional<ButtonType> res = ad.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            Assignment newA = new Assignment(sel.getCourseCode(), titleField.getText(), duePicker.getValue(), statusBox.getValue());
            allAssignments.add(newA);
            assignmentsPerCourse.computeIfAbsent(sel.getCourseCode(), k -> new ArrayList<>()).add(newA);
            saveAssignmentsToFile();
            assignmentsTable.getItems().add(newA);
        }
    });

    Button editAssignmentBtn = new Button("Edit Assignment");
    editAssignmentBtn.setOnAction(e -> {
        Assignment selA = assignmentsTable.getSelectionModel().getSelectedItem();
        Course selCourse = courseCombo.getSelectionModel().getSelectedItem();
        if (selA == null || selCourse == null) return;
        Dialog<ButtonType> ad = new Dialog<>();
        ad.setTitle("Edit Assignment");
        TextField titleField = new TextField(selA.getTitle());
        DatePicker duePicker = new DatePicker(selA.getDueDate());
        ComboBox<String> statusBox = new ComboBox<>(FXCollections.observableArrayList("Pending", "Completed", "Overdue"));
        statusBox.setValue(selA.getStatus());
        VBox vb = new VBox(new Label("Assignment Title"), titleField, new Label("Due Date"), duePicker, new Label("Status"), statusBox);
        ad.getDialogPane().setContent(vb);
        ad.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Optional<ButtonType> res = ad.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            int index = assignmentsTable.getItems().indexOf(selA);
            Assignment updated = new Assignment(selA.getCourseName(), titleField.getText(), duePicker.getValue(), statusBox.getValue());

            int indexInAll = allAssignments.indexOf(selA);
            if (indexInAll != -1) allAssignments.set(indexInAll, updated);
            List<Assignment> courseAssigns = assignmentsPerCourse.getOrDefault(selCourse.getCourseCode(), new ArrayList<>());
            int indexInCourse = courseAssigns.indexOf(selA);
            if (indexInCourse != -1) courseAssigns.set(indexInCourse, updated);

            saveAssignmentsToFile();
            assignmentsTable.getItems().set(index, updated);
            assignmentsTable.refresh();
        }
    });

    Button deleteAssignmentBtn = new Button("Delete Assignment");
    deleteAssignmentBtn.setOnAction(e -> {
        Assignment selA = assignmentsTable.getSelectionModel().getSelectedItem();
        Course selCourse = courseCombo.getSelectionModel().getSelectedItem();
        if (selA == null || selCourse == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete assignment \"" + selA.getTitle() + "\"?", ButtonType.OK, ButtonType.CANCEL);
        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            allAssignments.remove(selA);
            List<Assignment> courseAssigns = assignmentsPerCourse.getOrDefault(selCourse.getCourseCode(), new ArrayList<>());
            courseAssigns.remove(selA);
            saveAssignmentsToFile();
            assignmentsTable.getItems().remove(selA);
        }
    });

    HBox buttons = new HBox(10, addAssignmentBtn, editAssignmentBtn, deleteAssignmentBtn);

    VBox layout = new VBox(10, new Label("Select Course:"), courseCombo, assignmentsTable, buttons);
    dialog.getDialogPane().setContent(layout);
    dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
    dialog.showAndWait();
}
    private void showManageCourseContentDialog(InstructorUser instr) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Manage Course Content");
        dialog.setHeaderText("Edit course info for your assigned courses");

        List<Course> myCourses = new ArrayList<>();
        for (Course c : courseData) {
            if (instructorAssignments.getOrDefault(c.getCourseCode(), "").equals(instr.getUserId()))
                myCourses.add(c);
        }

        TableView<Course> table = new TableView<>(FXCollections.observableArrayList(myCourses));
        TableColumn<Course, String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        TableColumn<Course, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        table.getColumns().addAll(codeCol, nameCol);

        Button editBtn = new Button("Edit Course");
        editBtn.setOnAction(e -> {
            Course c = table.getSelectionModel().getSelectedItem();
            if (c != null) {
                Course updated = showCourseEditDialog(c);
                if (updated != null) {
                    int idx = courseData.indexOf(c);
                    courseData.set(idx, updated);
                    saveCoursesToFile();
                    showInfoDialog("Updated", null, "Course updated.");
                }
            }
        });

        dialog.getDialogPane().setContent(new VBox(10, table, editBtn));
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
    private void showReportsDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("System Report");
        dialog.setHeaderText("Data Report (Users, Courses, Enrollments)");

        StringBuilder sb = new StringBuilder();
        sb.append("Users: ").append(loadUsersFromFile().size()).append("\n");
        sb.append("Courses: ").append(courseData.size()).append("\n");
        sb.append("Enrollments:\n");
        for (Map.Entry<String, Set<String>> ent : enrollmentMap.entrySet()) {
            User u = findUserById(ent.getKey());
            sb.append("  ").append(u != null ? u.getName() : ent.getKey()).append(": ");
            for (String cid : ent.getValue()) {
                sb.append(cid).append(" ");
            }
            sb.append("\n");
        }
        sb.append("Instructor Assignments:\n");
        for (Map.Entry<String, String> ia : instructorAssignments.entrySet()) {
            Course c = courseData.stream().filter(x -> x.getCourseCode().equals(ia.getKey())).findFirst().orElse(null);
            User instr = findUserById(ia.getValue());
            sb.append("  ").append(c != null ? c.getCourseName() : ia.getKey()).append(": ");
            sb.append(instr != null ? instr.getName() : ia.getValue()).append("\n");
        }
        TextArea area = new TextArea(sb.toString());
        area.setPrefRowCount(16);
        dialog.getDialogPane().setContent(area);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void showAvailableCoursesDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Available Courses");
        dialog.setHeaderText("Course Catalog");

        TableView<Course> table = new TableView<>(courseData);
        TableColumn<Course, String> codeCol = new TableColumn<>("Course Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        TableColumn<Course, String> nameCol = new TableColumn<>("Course Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        TableColumn<Course, String> instrCol = new TableColumn<>("Instructor");
        instrCol.setCellValueFactory(new PropertyValueFactory<>("instructorName"));
        TableColumn<Course, Integer> creditsCol = new TableColumn<>("Credits");
        creditsCol.setCellValueFactory(new PropertyValueFactory<>("credits"));
        table.getColumns().addAll(codeCol, nameCol, instrCol, creditsCol);
        dialog.getDialogPane().setContent(new VBox(table));
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
private void showGradesDialog() {
    User student = authManager.getCurrentUser();
    Dialog<ButtonType> dialog = new Dialog<>();
    dialog.setTitle("My Grades");
    dialog.setHeaderText("Current Course Grades");
    TableView<CourseGrade> gradesTable = new TableView<>();

    TableColumn<CourseGrade, String> courseCol = new TableColumn<>("Course");
    courseCol.setCellValueFactory(new PropertyValueFactory<>("courseName"));

    TableColumn<CourseGrade, String> gradeCol = new TableColumn<>("Grade");
    gradeCol.setCellValueFactory(new PropertyValueFactory<>("grade"));

    TableColumn<CourseGrade, Double> percCol = new TableColumn<>("Percentage");
    percCol.setCellValueFactory(new PropertyValueFactory<>("percentage"));

    gradesTable.getColumns().addAll(courseCol, gradeCol, percCol);

    Map<String, CourseGrade> stuGrades = gradesPerStudent.getOrDefault(student.getUserId(), new HashMap<>());
    ObservableList<CourseGrade> grades = FXCollections.observableArrayList();
    double gpaTotal = 0;
    int gpaCount = 0;
    for (CourseGrade cg : stuGrades.values()) {
        String gradeLetter = cg.getGrade();
        double percent = cg.getPercentage();
        if (percent == 0.0 && gradeLetter != null) percent = GradeUtil.toPercentage(gradeLetter);
        grades.add(new CourseGrade(cg.getCourseName(), gradeLetter, percent));
        gpaTotal += GradeUtil.toGpa(gradeLetter);
        gpaCount++;
    }
    gradesTable.setItems(grades);

    double gpa = (gpaCount > 0) ? (gpaTotal / gpaCount) : 0.0;
    Label gpaLabel = new Label(String.format("Cumulative GPA: %.2f", gpa));
    gpaLabel.setStyle("-fx-font-weight: bold;");

    VBox layout = new VBox(10, gradesTable, gpaLabel);
    dialog.getDialogPane().setContent(layout);
    dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
    dialog.showAndWait();
}

private void showStudentAssignmentsDialog(Student student) {
    Dialog<ButtonType> dialog = new Dialog<>();
    dialog.setTitle("My Assignments");
    dialog.setHeaderText("Current and Upcoming Assignments");

    Set<String> enrolledCourses = enrollmentMap.getOrDefault(student.getUserId(), Collections.emptySet());
    List<Assignment> myAssignments = new ArrayList<>();
    for (String ccode : enrolledCourses) {
        myAssignments.addAll(assignmentsPerCourse.getOrDefault(ccode, Collections.emptyList()));
    }

    TableView<Assignment> assignmentsTable = new TableView<>(FXCollections.observableArrayList(myAssignments));
    TableColumn<Assignment, String> courseColumn = new TableColumn<>("Course");
    courseColumn.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
    TableColumn<Assignment, String> titleColumn = new TableColumn<>("Assignment Title");
    titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
    TableColumn<Assignment, LocalDate> dueDateColumn = new TableColumn<>("Due Date");
    dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
    TableColumn<Assignment, String> statusColumn = new TableColumn<>("Status");
    statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

    assignmentsTable.getColumns().addAll(courseColumn, titleColumn, dueDateColumn, statusColumn);

    Button viewDetailsButton = new Button("View Assignment Details");
    viewDetailsButton.setOnAction(e -> {
        Assignment selectedAssignment = assignmentsTable.getSelectionModel().getSelectedItem();
        if (selectedAssignment != null) {
            StringBuilder content = new StringBuilder();
            String ccode = selectedAssignment.getCourseCode();
            String courseName = ccode;
            String instructorName = "";
            for (Course c : courseData) {
                if (c.getCourseCode().equals(ccode)) {
                    courseName = c.getCourseName();
                    instructorName = getUserNameFromId(instructorAssignments.get(ccode));
                    break;
                }
            }
            LocalDate due = selectedAssignment.getDueDate();
            String status = selectedAssignment.getStatus();
            String warningMsg = "";
            String color = "-fx-text-fill: #333;";
            if (due != null) {
                long days = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), due);
                if (status.equalsIgnoreCase("Completed")) {
                    warningMsg = "✅ Completed";
                    color = "-fx-text-fill: #3ead33;";
                } else if (days < 0) {
                    warningMsg = "⚠️ Overdue by " + Math.abs(days) + " days";
                    color = "-fx-text-fill: #d63c3c;";
                } else if (days == 0) {
                    warningMsg = "⏰ Due Today!";
                    color = "-fx-text-fill: orange;";
                } else if (days <= 3) {
                    warningMsg = "⌛ Due in " + days + " day" + (days == 1 ? "" : "s");
                    color = "-fx-text-fill: #faca19;";
                } else {
                    warningMsg = "Due in " + days + " days";
                }
            }

            content.append("Title: ").append(selectedAssignment.getTitle()).append("\n")
                   .append("Course: ").append(courseName).append(" (").append(ccode).append(")\n")
                   .append("Instructor: ").append((instructorName != null) ? instructorName : "Unknown").append("\n")
                   .append("Due Date: ").append(due != null ? due : "N/A").append("\n")
                   .append("Status: ").append(status).append("\n");

            if (!warningMsg.isEmpty()) {
                content.append("\n").append(warningMsg);
            }

            Dialog<ButtonType> detailsDialog = new Dialog<>();
            detailsDialog.setTitle("Assignment Details");
            detailsDialog.setHeaderText(selectedAssignment.getTitle());
            TextArea detailArea = new TextArea(content.toString());
            detailArea.setEditable(false);
            detailArea.setStyle("-fx-background-color: #fbfbfb; -fx-font-size: 13px; " + color);
            detailArea.setWrapText(true);
            detailsDialog.getDialogPane().setContent(detailArea);
            detailsDialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
            detailsDialog.showAndWait();
        }
    });

    VBox layout = new VBox(10, assignmentsTable, viewDetailsButton);
    dialog.getDialogPane().setContent(layout);
    dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
    dialog.showAndWait();
}

    public static void main(String[] args) {
        launch(args);
    }
}


