package com.coursemanagement;

import java.util.ArrayList;
import java.util.List;

public class InstructorUser extends User {
    private List<String> coursesTaught;
    private String department;
    private String academicTitle;

    public InstructorUser(String userId, String name, String email, String password) {
        super(userId, name, email, password);
        this.coursesTaught = new ArrayList<>();
        this.department = "Unassigned";
        this.academicTitle = "Instructor";
    }

    public InstructorUser(String userId, String name, String email, String password, 
                          String department, String academicTitle) {
        super(userId, name, email, password);
        this.coursesTaught = new ArrayList<>();
        this.department = department;
        this.academicTitle = academicTitle;
    }

    @Override
    public String getUserType() {
        return "INSTRUCTOR";
    }

    public void assignCourse(String courseId) {
        if (!coursesTaught.contains(courseId)) {
            coursesTaught.add(courseId);
        }
    }

    public void removeCourse(String courseId) {
        coursesTaught.remove(courseId);
    }

    public List<String> getCoursesTaught() {
        return new ArrayList<>(coursesTaught);
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getAcademicTitle() {
        return academicTitle;
    }

    public void setAcademicTitle(String academicTitle) {
        this.academicTitle = academicTitle;
    }

    @Override
    public String toString() {
        return "InstructorUser{" +
                "userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", department='" + department + '\'' +
                ", academicTitle='" + academicTitle + '\'' +
                ", coursesTaught=" + coursesTaught +
                '}';
    }
}