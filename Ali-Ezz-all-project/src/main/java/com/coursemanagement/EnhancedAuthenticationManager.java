package com.coursemanagement;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class EnhancedAuthenticationManager extends AuthenticationManager {
  private static final int MAX_LOGIN_ATTEMPTS = 3;
  
  private static final int LOCKOUT_DURATION = 15;
  
  private Map<String, LoginAttempt> loginAttempts = new HashMap<>();
  

  private String hashPassword(String password) {
      try {
          MessageDigest digest = MessageDigest.getInstance("SHA-256");
          byte[] hash = digest.digest(password.getBytes());
          return Base64.getEncoder().encodeToString(hash);
      } catch (NoSuchAlgorithmException e) {
          LoggingUtil.logError("Password hashing failed", e);
          return null;
      }
  }
  

  @Override
  public boolean authenticate(String email, String password) {
      LoginAttempt attempt = loginAttempts.get(email);
      if (attempt != null && attempt.isLocked()) {
          LoggingUtil.logWarning("Account locked: " + email);
          return false;
      }
      
      String hashedPassword = hashPassword(password);
      
      User user = userDatabase.get(email);
      boolean isAuthenticated = user != null && user.password.equals(hashedPassword);
      
      updateLoginAttempts(email, isAuthenticated);
      
      return isAuthenticated;
  }
  

  private void updateLoginAttempts(String email, boolean isAuthenticated) {
      LoginAttempt attempt = loginAttempts.getOrDefault(email, new LoginAttempt());
      
      if (isAuthenticated) {
          loginAttempts.remove(email);
      } else {
          attempt.incrementAttempts();
          loginAttempts.put(email, attempt);
      }
  }
  

  private static class LoginAttempt {
      private int attempts = 0;
      private LocalDateTime lastAttempt;
      
      public void incrementAttempts() {
          attempts++;
          lastAttempt = LocalDateTime.now();
      }
      
      public boolean isLocked() {
          if (attempts >= MAX_LOGIN_ATTEMPTS) {
              return lastAttempt.plusMinutes(LOCKOUT_DURATION).isAfter(LocalDateTime.now());
          }
          return false;
      }
  }
}