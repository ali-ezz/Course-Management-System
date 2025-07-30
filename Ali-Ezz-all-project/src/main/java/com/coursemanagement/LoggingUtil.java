package com.coursemanagement;

import java.util.logging.*;
import java.io.*;

public class LoggingUtil {
  private static final Logger LOGGER = Logger.getLogger(LoggingUtil.class.getName());
  
  static {
      try {
          File logsDir = new File("logs");
          if (!logsDir.exists()) {
              logsDir.mkdir();
          }
          
          FileHandler fileHandler = new FileHandler("logs/course_management.log", true);
          fileHandler.setFormatter(new SimpleFormatter());
          
          ConsoleHandler consoleHandler = new ConsoleHandler();
          consoleHandler.setFormatter(new SimpleFormatter());
          
          LOGGER.setLevel(Level.ALL);
          LOGGER.addHandler(fileHandler);
          LOGGER.addHandler(consoleHandler);
          
          LOGGER.setUseParentHandlers(false);
      } catch (IOException e) {
          System.err.println("Could not create log file: " + e.getMessage());
      }
  }
  

  public static void logInfo(String message) {
      LOGGER.info(message);
  }
  

  public static void logWarning(String message) {
      LOGGER.warning(message);
  }
  

  public static void logError(String message, Throwable throwable) {
      LOGGER.log(Level.SEVERE, message, throwable);
  }
  

  public static void logDebug(String message) {
      LOGGER.fine(message);
  }
}