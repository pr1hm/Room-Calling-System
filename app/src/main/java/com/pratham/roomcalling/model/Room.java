package com.pratham.roomcalling.model;

public class Room {
    private String roomId;
    private String roomName;
    private String stationName;
    private int status;
    private int priority;
    private String roomGroup;

    public Room(String roomId, String roomName, String stationName, int status, int priority, String roomGroup) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.stationName = stationName;
        this.status = status;
        this.priority = priority;
        this.roomGroup = roomGroup;
    }

    public String getRoomId() { return roomId; }
    public String getRoomName() { return roomName; }
    public String getStationName() { return stationName; }
    public int getStatus() { return status; }
    public int getPriority() { return priority; }
    public String getRoomGroup() { return roomGroup; }
}