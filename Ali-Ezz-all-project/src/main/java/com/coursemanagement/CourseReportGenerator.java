package com.coursemanagement;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CourseReportGenerator {
  private List<Course> courses;
  
  public CourseReportGenerator(List<Course> courses) {
      this.courses = courses;
  }
  
  public List<Course> getUpcomingCourses(int daysThreshold) {
      LocalDate today = LocalDate.now();
      return courses.stream()
          .filter(course -> 
              course.getStartDate().isAfter(today) && 
              course.getStartDate().isBefore(today.plusDays(daysThreshold)))
          .collect(Collectors.toList());
  }
  
  public List<Course> getEndingCourses(int daysThreshold) {
      LocalDate today = LocalDate.now();
      return courses.stream()
          .filter(course -> 
              course.getEndDate().isAfter(today) && 
              course.getEndDate().isBefore(today.plusDays(daysThreshold)))
          .collect(Collectors.toList());
  }
}

