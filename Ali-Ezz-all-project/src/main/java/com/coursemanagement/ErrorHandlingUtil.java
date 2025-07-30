package com.coursemanagement;


import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.Optional;

public class ErrorHandlingUtil {

  public static void showErrorDialog(String title, String header, String content) {
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle(title);
      alert.setHeaderText(header);
      alert.setContentText(content);
      
      LoggingUtil.logError(title + ": " + content, null);
      
      alert.showAndWait();
  }
  

  public static boolean showConfirmationDialog(String title, String header, String content) {
      Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
      alert.setTitle(title);
      alert.setHeaderText(header);
      alert.setContentText(content);
      
      Optional<ButtonType> result = alert.showAndWait();
      return result.isPresent() && result.get() == ButtonType.OK;
  }
  

  public static class CourseManagementException extends Exception {
      public CourseManagementException(String message) {
          super(message);
          LoggingUtil.logError(message, this);
      }
      
      public CourseManagementException(String message, Throwable cause) {
          super(message, cause);
          LoggingUtil.logError(message, cause);
      }
  }
}