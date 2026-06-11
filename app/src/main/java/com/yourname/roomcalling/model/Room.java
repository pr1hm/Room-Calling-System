package com.yourname.roomcalling.model;

public class Room {
    private String roomId;
    private String roomName;
    private String stationName;
    private int status;
    private String lastUpdated;

    public Room() {}

    public Room(String roomId, String roomName, String stationName, int status, String lastUpdated) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.stationName = stationName;
        this.status = status;
        this.lastUpdated = lastUpdated;
    }

    // Getters and Setters
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(String lastUpdated) { this.lastUpdated = lastUpdated; }
}