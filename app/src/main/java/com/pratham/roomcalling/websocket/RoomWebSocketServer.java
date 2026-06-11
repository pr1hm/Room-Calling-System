package com.pratham.roomcalling.websocket;

import android.util.Log;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import java.net.InetSocketAddress;

public class RoomWebSocketServer extends WebSocketServer {

    private static final String TAG = "WS_SERVER";

    public RoomWebSocketServer() {
        // The SRS dictates WebSockets run on port 8090
        super(new InetSocketAddress(8090));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        Log.d(TAG, "New connection from: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        Log.d(TAG, "Connection closed: " + conn.getRemoteSocketAddress() + " Reason: " + reason);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        Log.d(TAG, "Message from client: " + message);
        // We will handle the "GETDETAIL" message from clients later
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        Log.e(TAG, "An error occurred", ex);
    }

    @Override
    public void onStart() {
        Log.d(TAG, "WebSocket server started successfully on port 8090");
    }

    // Custom method to broadcast JSON to all connected screens
    public void broadcastUpdate(String jsonPayload) {
        for (WebSocket conn : getConnections()) {
            if (conn.isOpen()) {
                conn.send(jsonPayload);
            }
        }
        Log.d(TAG, "Broadcasted update to " + getConnections().size() + " clients.");
    }
}