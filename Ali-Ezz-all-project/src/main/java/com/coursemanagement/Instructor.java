package com.coursemanagement;

import java.util.ArrayList;
import java.util.List;

public class Instructor extends User {
  private static final long serialVersionUID = 1L;

  private List<Course> assignedCourses;

  public Instructor(String userId, String name, String email, String password) {
      super(userId, name, email, password);
      this.assignedCourses = new ArrayList<>();
  }

  @Override
  public String getUserType() {
      return "INSTRUCTOR";
  }

  public void assignCourse(Course course) {
      if (!assignedCourses.contains(course)) {
          assignedCourses.add(course);
      }
  }

  public List<Course> getAssignedCourses() {
      return new ArrayList<>(assignedCourses);
  }
}
