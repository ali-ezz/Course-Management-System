package com.coursemanagement;

import java.time.LocalDate;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class ValidationUtil {
  private static final String EMAIL_REGEX = 
      "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
  
  private static final String PASSWORD_REGEX = 
      "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,20}$";
  
  private static final String NAME_REGEX = "^[a-zA-Z\\s-]{2,50}$";
  
  public static boolean validateEmail(String email) {
      if (email == null) return false;
      Pattern pattern = Pattern.compile(EMAIL_REGEX);
      Matcher matcher = pattern.matcher(email);
      return matcher.matches();
  }
  

  public static boolean validatePassword(String password) {
      if (password == null) return false;
      Pattern pattern = Pattern.compile(PASSWORD_REGEX);
      Matcher matcher = pattern.matcher(password);
      return matcher.matches();
  }
  

  public static boolean validateName(String name) {
      if (name == null) return false;
      Pattern pattern = Pattern.compile(NAME_REGEX);
      Matcher matcher = pattern.matcher(name);
      return matcher.matches();
  }
  

  public static boolean validatePrice(double price) {
      return price >= 0 && price <= 10000;
  }
  

  public static boolean validateCourseDates(LocalDate startDate, LocalDate endDate) {
      return startDate != null && 
             endDate != null && 
             !startDate.isAfter(endDate) && 
             !startDate.isBefore(LocalDate.now());
  }
}