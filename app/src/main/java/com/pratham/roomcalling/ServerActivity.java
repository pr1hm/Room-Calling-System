package com.pratham.roomcalling;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.pratham.roomcalling.db.DatabaseHelper;
import com.pratham.roomcalling.http.DashboardServer;
import com.pratham.roomcalling.websocket.RoomWebSocketServer;
import java.io.IOException;

public class ServerActivity extends AppCompatActivity {

    private DashboardServer dashboardServer;
    private RoomWebSocketServer webSocketServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        // Seed the database
        DatabaseHelper db = new DatabaseHelper(this);
        db.addRoom("101", "Room 101 - Test Patient", "Ward A");

        // Start WebSocket Server first
        webSocketServer = new RoomWebSocketServer(db);
        webSocketServer.start();
        Log.d("WS_SERVER", "Attempting to start WebSocket Server on port 8090...");

        // Start HTTP Server and pass the WebSocket server to it
        dashboardServer = new DashboardServer(this, webSocketServer);
        try {
            dashboardServer.start();
            Log.d("HTTP_TEST", "HTTP Server started on port 8080");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("HTTP_TEST", "Could not start HTTP server", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Always shut down servers to free up ports
        if (dashboardServer != null) {
            dashboardServer.stop();
            Log.d("HTTP_TEST", "HTTP Server stopped");
        }

        if (webSocketServer != null) {
            try {
                webSocketServer.stop();
                Log.d("WS_SERVER", "WebSocket Server stopped");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}