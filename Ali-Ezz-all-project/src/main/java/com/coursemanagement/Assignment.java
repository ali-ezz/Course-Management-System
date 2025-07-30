package com.coursemanagement;

import java.time.LocalDate;

public class Assignment {
    private String courseName;
    private String title;
    private LocalDate dueDate;
    private String status;

    public Assignment(String courseName, String title, LocalDate dueDate, String status) {
        this.courseName = courseName;
        this.title = title;
        this.dueDate = dueDate;
        this.status = status;
    }

    public String getCourseName() { return courseName; }
    public String getTitle() { return title; }
    public LocalDate getDueDate() { return dueDate; }
    public String getStatus() { return status; }

    String getCourseCode() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }
}