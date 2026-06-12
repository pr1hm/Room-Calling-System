package com.pratham.roomcalling;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.pratham.roomcalling.adapter.RoomAdapter;
import com.pratham.roomcalling.model.Room;
import com.pratham.roomcalling.websocket.RoomWebSocketClient;
import com.pratham.roomcalling.util.RoomAudioManager;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;

public class ClientActivity extends AppCompatActivity {

    private RoomWebSocketClient wsClient;
    private RoomAudioManager audioManager;
    private EditText etServerIp;
    private LinearLayout layoutLogin, layoutGrid;
    private RecyclerView recyclerViewRooms;
    private RoomAdapter roomAdapter;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        gson = new Gson();
        audioManager = new RoomAudioManager(this);

        // UI References
        etServerIp = findViewById(R.id.etServerIp);
        Button btnConnect = findViewById(R.id.btnConnect);
        layoutLogin = findViewById(R.id.layoutLogin);
        layoutGrid = findViewById(R.id.layoutGrid);
        recyclerViewRooms = findViewById(R.id.recyclerViewRooms);

        // Setup Grid (2 columns)
        recyclerViewRooms.setLayoutManager(new GridLayoutManager(this, 2));
        roomAdapter = new RoomAdapter();
        recyclerViewRooms.setAdapter(roomAdapter);

        btnConnect.setOnClickListener(v -> connectToServer());
    }

    private void connectToServer() {
        String ipAddress = etServerIp.getText().toString().trim();
        if (ipAddress.isEmpty()) {
            Toast.makeText(this, "Please enter an IP address", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            URI uri = new URI("ws://" + ipAddress + ":8090");
            wsClient = new RoomWebSocketClient(uri);

            // Listen for incoming messages AND connection events
            wsClient.setWebSocketListener(new RoomWebSocketClient.WebSocketListener() {

                @Override
                public void onConnected() {
                    // Must run on UI thread because it's coming from a background network thread
                    runOnUiThread(() -> Toast.makeText(ClientActivity.this, "Successfully Connected!", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onDisconnected(String reason) {
                    runOnUiThread(() -> Toast.makeText(ClientActivity.this, "Connection Lost/Failed: " + reason, Toast.LENGTH_LONG).show());
                }

                @Override
                public void onMessageReceived(String message) {
                    try {
                        JsonObject jsonObject = gson.fromJson(message, JsonObject.class);
                        String action = jsonObject.get("action").getAsString();

                        // If it's our REFRESH_ALL payload
                        if ("REFRESH_ALL".equals(action)) {
                            JsonArray dataArray = jsonObject.getAsJsonArray("data");
                            Type listType = new TypeToken<List<Room>>(){}.getType();
                            List<Room> parsedRooms = gson.fromJson(dataArray, listType);

                            // CRITICAL: Move back to the Main Thread to update the UI
                            runOnUiThread(() -> {
                                // Hide login, show grid
                                layoutLogin.setVisibility(View.GONE);
                                layoutGrid.setVisibility(View.VISIBLE);

                                // Update the visual cards
                                roomAdapter.updateRooms(parsedRooms);

                                // Trigger the Audio Engine
                                audioManager.evaluateAndPlay(parsedRooms);
                            });
                        }
                    } catch (Exception e) {
                        Log.e("CLIENT_TEST", "Error parsing message", e);
                    }
                }
            });

            wsClient.connect();
            Toast.makeText(this, "Connecting...", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e("CLIENT_TEST", "Invalid URI", e);
            Toast.makeText(this, "Error formatting IP address", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (wsClient != null && wsClient.isOpen()) {
            wsClient.close();
        }
        // Stop audio when app closes
        if (audioManager != null) {
            audioManager.stopAudio();
        }
    }
}