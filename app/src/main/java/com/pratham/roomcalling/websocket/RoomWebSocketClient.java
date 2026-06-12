package com.pratham.roomcalling.websocket;

import android.util.Log;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;

public class RoomWebSocketClient extends WebSocketClient {

    private static final String TAG = "CLIENT_TEST";

    // Expanded interface to handle connection success and failure
    public interface WebSocketListener {
        void onMessageReceived(String message);
        void onConnected();
        void onDisconnected(String reason);
    }

    private WebSocketListener listener;

    public RoomWebSocketClient(URI serverUri) {
        super(serverUri);
    }

    public void setWebSocketListener(WebSocketListener listener) {
        this.listener = listener;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Log.d(TAG, "Connected to Server!");
        if (listener != null) listener.onConnected(); // Trigger Success Toast
        send("GETDETAIL");
    }

    @Override
    public void onMessage(String message) {
        Log.d(TAG, "Received update: " + message);
        if (listener != null) {
            listener.onMessageReceived(message); // Trigger UI Update
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.d(TAG, "Disconnected from server. Reason: " + reason);
        if (listener != null) listener.onDisconnected(reason); // Trigger Fail/Disconnect Toast
    }

    @Override
    public void onError(Exception ex) {
        Log.e(TAG, "WebSocket Client Error", ex);
        if (listener != null) listener.onDisconnected(ex.getMessage()); // Trigger Error Toast
    }
}