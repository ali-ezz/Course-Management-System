package com.coursemanagement;


public class CourseGrade {
    private String courseName;
    private String grade;
    private double percentage;

    public CourseGrade(String courseName, String grade, double percentage) {
        this.courseName = courseName;
        this.grade = grade;
        this.percentage = percentage;
    }

    public String getCourseName() { return courseName; }
    public String getGrade() { return grade; }
    public double getPercentage() { return percentage; }
}
