package com.pratham.roomcalling.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.pratham.roomcalling.model.Room;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "RoomCalling.db";
    private static final int DATABASE_VERSION = 3; // Upgraded to support Groups & Priority

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE rooms (roomId TEXT PRIMARY KEY, roomName TEXT, stationName TEXT, status INTEGER, priority INTEGER DEFAULT 0, roomGroup TEXT DEFAULT 'None')");
        db.execSQL("CREATE TABLE settings (setting_key TEXT PRIMARY KEY, setting_value TEXT)");
        db.execSQL("CREATE TABLE call_logs (logId INTEGER PRIMARY KEY AUTOINCREMENT, roomId TEXT, status INTEGER, timestamp TEXT)");

        db.execSQL("INSERT INTO settings (setting_key, setting_value) VALUES ('columns', '2')");
        db.execSQL("INSERT INTO settings (setting_key, setting_value) VALUES ('sound_enabled', '1')");
        db.execSQL("INSERT INTO settings (setting_key, setting_value) VALUES ('sound_repeats', '1')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE rooms ADD COLUMN priority INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE rooms ADD COLUMN roomGroup TEXT DEFAULT 'None'");
        }
    }

    // --- IOT ROOM CRUD ---
    public void addOrUpdateRoom(String roomId, String roomName) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("INSERT INTO rooms (roomId, roomName, stationName, status, priority, roomGroup) VALUES (?, ?, 'Default', 0, 0, 'None') " +
                "ON CONFLICT(roomId) DO UPDATE SET roomName=?", new String[]{roomId, roomName, roomName});
        db.close();
    }

    public void deleteRoom(String roomId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("rooms", "roomId=?", new String[]{roomId});
        db.close();
    }

    public void updateStatus(String roomId, int status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", status);
        db.update("rooms", values, "roomId=?", new String[]{roomId});

        if (status > 0) {
            String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            ContentValues logValues = new ContentValues();
            logValues.put("roomId", roomId);
            logValues.put("status", status);
            logValues.put("timestamp", time);
            db.insert("call_logs", null, logValues);
        }
        db.close();
    }

    // NEW: Fetches rooms sorted by Status FIRST, then Priority SECOND
    public List<Room> getAllRooms() {
        List<Room> roomList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        // Priority tiering logic implemented here!
        Cursor cursor = db.rawQuery("SELECT * FROM rooms ORDER BY status DESC, priority DESC", null);
        if (cursor.moveToFirst()) {
            do {
                Room room = new Room(
                        cursor.getString(0), cursor.getString(1), cursor.getString(2),
                        cursor.getInt(3), cursor.getInt(4), cursor.getString(5));
                roomList.add(room);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return roomList;
    }

    // --- NEW: PRIORITY & GROUPING ---
    public void setPriorityList(String[] roomIds) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE rooms SET priority = 0"); // Reset all
        for (String id : roomIds) {
            db.execSQL("UPDATE rooms SET priority = 1 WHERE roomId = ?", new String[]{id.trim()});
        }
        db.close();
    }

    public void setRoomGroup(String groupId, String[] roomIds) {
        SQLiteDatabase db = this.getWritableDatabase();
        for (String id : roomIds) {
            db.execSQL("UPDATE rooms SET roomGroup = ? WHERE roomId = ?", new String[]{groupId.trim(), id.trim()});
        }
        db.close();
    }

    public void deleteRoomGroup(String groupId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE rooms SET roomGroup = 'None' WHERE roomGroup = ?", new String[]{groupId.trim()});
        db.close();
    }

    public String getRoomGroupListText() {
        StringBuilder sb = new StringBuilder();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT roomGroup, GROUP_CONCAT(roomId, ', ') FROM rooms WHERE roomGroup != 'None' GROUP BY roomGroup", null);
        if (cursor.moveToFirst()) {
            do {
                sb.append("Group ").append(cursor.getString(0)).append(" : [").append(cursor.getString(1)).append("]\n");
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return sb.length() > 0 ? sb.toString() : "No groups configured.";
    }

    // --- SETTINGS & LOGS ---
    public void saveSetting(String key, String value) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("setting_key", key);
        values.put("setting_value", value);
        db.replace("settings", null, values);
        db.close();
    }

    public String getSetting(String key, String defaultValue) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT setting_value FROM settings WHERE setting_key=?", new String[]{key});
        String value = defaultValue;
        if (cursor.moveToFirst()) value = cursor.getString(0);
        cursor.close();
        db.close();
        return value;
    }

    public List<String[]> getCallLogs() {
        List<String[]> logs = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT roomId, status, timestamp FROM call_logs ORDER BY logId DESC", null);
        if (cursor.moveToFirst()) {
            do {
                logs.add(new String[]{cursor.getString(0), String.valueOf(cursor.getInt(1)), cursor.getString(2)});
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return logs;
    }
}