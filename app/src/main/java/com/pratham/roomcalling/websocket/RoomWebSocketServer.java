package com.pratham.roomcalling.websocket;

import android.util.Log;
import com.google.gson.Gson;
import com.pratham.roomcalling.db.DatabaseHelper;
import com.pratham.roomcalling.model.Room;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomWebSocketServer extends WebSocketServer {

    private static final String TAG = "WS_SERVER";
    private final DatabaseHelper dbHelper;
    private final Gson gson;

    // Now requires DatabaseHelper to fetch rooms
    public RoomWebSocketServer(DatabaseHelper dbHelper) {
        super(new InetSocketAddress(8090));
        this.dbHelper = dbHelper;
        this.gson = new Gson();
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        Log.d(TAG, "New connection from: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        Log.d(TAG, "Connection closed: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        Log.d(TAG, "Message from client: " + message);

        // When client connects, it asks for the initial list of rooms
        if (message.equals("GETDETAIL")) {
            List<Room> allRooms = dbHelper.getAllRooms();
            Map<String, Object> payload = new HashMap<>();
            payload.put("action", "REFRESH_ALL");
            payload.put("station", "Ward A");
            payload.put("data", allRooms);

            // Send ONLY to the client that just asked
            conn.send(gson.toJson(payload));
            Log.d(TAG, "Sent GETDETAIL response to " + conn.getRemoteSocketAddress());
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        Log.e(TAG, "An error occurred", ex);
    }

    @Override
    public void onStart() {
        Log.d(TAG, "WebSocket server started successfully on port 8090");
    }

    public void broadcastUpdate(String jsonPayload) {
        for (WebSocket conn : getConnections()) {
            if (conn.isOpen()) {
                conn.send(jsonPayload);
            }
        }
    }
}