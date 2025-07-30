package com.coursemanagement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Student extends User {
  private static final long serialVersionUID = 1L;

  private List<Course> enrolledCourses;
  private Map<Course, Double> courseGrades;

  public Student(String userId, String name, String email, String password) {
      super(userId, name, email, password);
      this.enrolledCourses = new ArrayList<>();
      this.courseGrades = new HashMap<>();
  }

  @Override
  public String getUserType() {
      return "STUDENT";
  }

  public void enrollCourse(Course course) {
      if (!enrolledCourses.contains(course)) {
          enrolledCourses.add(course);
      }
  }

  public void setGrade(Course course, double grade) {
      courseGrades.put(course, grade);
  }

  public List<Course> getEnrolledCourses() {
      return new ArrayList<>(enrolledCourses);
  }

  public Map<Course, Double> getCourseGrades() {
      return new HashMap<>(courseGrades);
  }
}
