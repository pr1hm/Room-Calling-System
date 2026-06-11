package com.yourname.roomcalling.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.yourname.roomcalling.model.Room;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "roomcalling.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE_ROOMS = "rooms";
    public static final String COL_ROOM_ID = "room_id";
    public static final String COL_ROOM_NAME = "room_name";
    public static final String COL_STATION_NAME = "station_name";
    public static final String COL_STATUS = "status";
    public static final String COL_LAST_UPDATED = "last_updated";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_ROOMS + " ("
                + COL_ROOM_ID + " TEXT PRIMARY KEY, "
                + COL_ROOM_NAME + " TEXT NOT NULL, "
                + COL_STATION_NAME + " TEXT NOT NULL, "
                + COL_STATUS + " INTEGER DEFAULT 0, "
                + COL_LAST_UPDATED + " TEXT"
                + ")";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROOMS);
        onCreate(db);
    }

    private String getCurrentTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    public boolean addRoom(String roomId, String roomName, String stationName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ROOM_ID, roomId);
        values.put(COL_ROOM_NAME, roomName);
        values.put(COL_STATION_NAME, stationName);
        values.put(COL_STATUS, 0);
        values.put(COL_LAST_UPDATED, getCurrentTimestamp());
        long result = db.insert(TABLE_ROOMS, null, values);
        return result != -1;
    }

    public List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ROOMS, null, null, null, null, null, COL_STATION_NAME);
        if (cursor.moveToFirst()) {
            do {
                Room room = new Room(
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_ROOM_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_ROOM_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_STATION_NAME)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_STATUS)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_LAST_UPDATED))
                );
                rooms.add(room);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return rooms;
    }
}