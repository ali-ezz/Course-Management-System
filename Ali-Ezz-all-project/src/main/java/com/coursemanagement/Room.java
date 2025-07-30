package com.coursemanagement;

import java.io.Serializable;


public class Room implements Serializable {
  private static final long serialVersionUID = 1L;

  private String roomId;
  private int capacity;
  private String location;

public Room(String roomId, int capacity, String location) {
      this.roomId = roomId;
      this.capacity = capacity;
      this.location = location;
  }

  public String getRoomId() { return roomId; }
  public int getCapacity() { return capacity; }
  public String getLocation() { return location; }
}