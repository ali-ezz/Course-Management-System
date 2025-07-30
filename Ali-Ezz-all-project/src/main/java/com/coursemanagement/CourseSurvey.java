package com.coursemanagement;

import java.util.HashMap;
import java.util.Map;

public class CourseSurvey {
  private Course course;
  private Map<String, Integer> surveyResponses;
  
  public CourseSurvey(Course course) {
      this.course = course;
      this.surveyResponses = new HashMap<>();
  }
  
  public void addResponse(String question, int rating) {
      surveyResponses.put(question, rating);
  }
  
  public double getAverageRating() {
      return surveyResponses.values().stream()
          .mapToInt(Integer::intValue)
          .average()
          .orElse(0.0);
  }
}
