package com.coursemanagement;


import java.io.*;
import java.util.*;

public class FileStorageManager<T> {
  public void saveToFile(List<T> items, String filename) {
      try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
          oos.writeObject(items);
      } catch (IOException e) {
          e.printStackTrace();
      }
  }
  
  public List<T> loadFromFile(String filename) {
      try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
          return (List<T>) ois.readObject();
      } catch (IOException | ClassNotFoundException e) {
          return new ArrayList<>();
      }
  }
}

