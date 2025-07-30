package com.coursemanagement;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class Course implements Serializable {
  private static final long serialVersionUID = 1L;

  private String courseId;
  private String name;
  private Instructor instructor;
  private String branch;
  private double price;
  private String parentCourse;
  private List<Student> enrolledStudents;
  private Room assignedRoom;
  private LocalDate startDate;
  private LocalDate endDate;
  private List<String> courseDays;

  public Course(String courseId, String name, Instructor instructor, 
                String branch, double price, String parentCourse, 
                Room assignedRoom, LocalDate startDate, LocalDate endDate,
                List<String> courseDays) {
      this.courseId = courseId;
      this.name = name;
      this.instructor = instructor;
      this.branch = branch;
      this.price = price;
      this.parentCourse = parentCourse;
      this.assignedRoom = assignedRoom;
      this.startDate = startDate;
      this.endDate = endDate;
      this.courseDays = courseDays;
      this.enrolledStudents = new ArrayList<>();
  }

  public String getCourseId() { return courseId; }
  public String getName() { return name; }
  public Instructor getInstructor() { return instructor; }
  public double getPrice() { return price; }
  public LocalDate getStartDate() { return startDate; }
  public LocalDate getEndDate() { return endDate; }

  public void enrollStudent(Student student) {
      if (!enrolledStudents.contains(student)) {
          enrolledStudents.add(student);
      }
  }

  public List<Student> getEnrolledStudents() {
      return new ArrayList<>(enrolledStudents);
  }

    private String courseCode;
    private String courseName;
    private String instructorName;
    private int credits;

    public Course(String courseCode, String courseName, String instructorName, int credits) {
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.instructorName = instructorName;
        this.credits = credits;
    }

    public String getCourseCode() { return courseCode; }
    public String getCourseName() { return courseName; }
    public String getInstructorName() { return instructorName; }
    public int getCredits() { return credits; }
}




 