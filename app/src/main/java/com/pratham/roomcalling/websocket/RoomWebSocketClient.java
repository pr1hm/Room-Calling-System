package com.pratham.roomcalling.websocket;

import android.util.Log;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;

public class RoomWebSocketClient extends WebSocketClient {

    private static final String TAG = "CLIENT_TEST";
    private MessageListener messageListener;

    // Interface to talk back to the Activity
    public interface MessageListener {
        void onMessageReceived(String message);
    }

    public RoomWebSocketClient(URI serverUri) {
        super(serverUri);
    }

    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Log.d(TAG, "Connected to Server!");
        send("GETDETAIL");
    }

    @Override
    public void onMessage(String message) {
        Log.d(TAG, "Received update: " + message);
        // Pass the message to the UI
        if (messageListener != null) {
            messageListener.onMessageReceived(message);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.d(TAG, "Disconnected from server. Reason: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        Log.e(TAG, "WebSocket Client Error", ex);
    }
}