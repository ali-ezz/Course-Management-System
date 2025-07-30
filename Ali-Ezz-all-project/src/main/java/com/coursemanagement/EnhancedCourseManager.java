package com.coursemanagement;

import java.util.ArrayList;
import java.util.List;


public class EnhancedCourseManager {
  private List<Course> courses;
  private FileStorageManager<Course> storageManager;
  
  public EnhancedCourseManager() {
      this.courses = new ArrayList<>();
      this.storageManager = new FileStorageManager<>();
      loadCourses();
  }
  

  public void addCourse(Course course) throws ErrorHandlingUtil.CourseManagementException {
      validateCourse(course);
      
      if (courses.stream().anyMatch(c -> c.getCourseId().equals(course.getCourseId()))) {
          throw new ErrorHandlingUtil.CourseManagementException("Course with this ID already exists");
      }
      
      courses.add(course);
      
      LoggingUtil.logInfo("Course added: " + course.getName());
      
      saveCourses();
  }
  

  private void validateCourse(Course course) throws ErrorHandlingUtil.CourseManagementException {
      if (!ValidationUtil.validateName(course.getName())) {
          throw new ErrorHandlingUtil.CourseManagementException("Invalid course name");
      }
      
      if (!ValidationUtil.validatePrice(course.getPrice())) {
          throw new ErrorHandlingUtil.CourseManagementException("Invalid course price");
      }
      
      if (!ValidationUtil.validateCourseDates(course.getStartDate(), course.getEndDate())) {
          throw new ErrorHandlingUtil.CourseManagementException("Invalid course dates");
      }
      
  }
  

  private void saveCourses() {
      storageManager.saveToFile(courses, "courses.ser");
  }
  
 
  private void loadCourses() {
      courses = storageManager.loadFromFile("courses.ser");
  }
}