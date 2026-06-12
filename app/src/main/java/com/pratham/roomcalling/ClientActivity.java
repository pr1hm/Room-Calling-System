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
import com.pratham.roomcalling.util.RoomAudioManager;
import com.pratham.roomcalling.websocket.RoomWebSocketClient;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class ClientActivity extends AppCompatActivity {

    private RoomWebSocketClient wsClient;
    private EditText etServerIp, etStationName; // Added etStationName
    private LinearLayout layoutLogin, layoutGrid;
    private RecyclerView recyclerViewRooms;
    private RoomAdapter roomAdapter;
    private RoomAudioManager audioManager;
    private Gson gson;
    private String targetStation = ""; // Holds the user's ward choice

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        gson = new Gson();
        audioManager = new RoomAudioManager(this);

        // UI References
        etServerIp = findViewById(R.id.etServerIp);
        etStationName = findViewById(R.id.etStationName); // Initialize new input
        Button btnConnect = findViewById(R.id.btnConnect);
        layoutLogin = findViewById(R.id.layoutLogin);
        layoutGrid = findViewById(R.id.layoutGrid);
        recyclerViewRooms = findViewById(R.id.recyclerViewRooms);

        // Setup Grid
        recyclerViewRooms.setLayoutManager(new GridLayoutManager(this, 2));
        roomAdapter = new RoomAdapter();
        recyclerViewRooms.setAdapter(roomAdapter);

        btnConnect.setOnClickListener(v -> connectToServer());
    }

    private void connectToServer() {
        String ipAddress = etServerIp.getText().toString().trim();
        targetStation = etStationName.getText().toString().trim();

        if (ipAddress.isEmpty() || targetStation.isEmpty()) {
            Toast.makeText(this, "Please enter both IP and Station Name", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            URI uri = new URI("ws://" + ipAddress + ":8090");
            wsClient = new RoomWebSocketClient(uri);

            wsClient.setWebSocketListener(new RoomWebSocketClient.WebSocketListener() {
                @Override
                public void onConnected() {
                    runOnUiThread(() -> Toast.makeText(ClientActivity.this, "Successfully Connected!", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onDisconnected(String reason) {
                    runOnUiThread(() -> Toast.makeText(ClientActivity.this, "Connection Failed: " + reason, Toast.LENGTH_LONG).show());
                }

                @Override
                public void onMessageReceived(String message) {
                    try {
                        JsonObject jsonObject = gson.fromJson(message, JsonObject.class);
                        String action = jsonObject.get("action").getAsString();

                        if ("REFRESH_ALL".equals(action)) {
                            JsonArray dataArray = jsonObject.getAsJsonArray("data");
                            Type listType = new TypeToken<List<Room>>(){}.getType();
                            List<Room> allRooms = gson.fromJson(dataArray, listType);

                            // --- NEW: FILTER THE DATA ---
                            List<Room> myStationRooms = new ArrayList<>();
                            for (Room room : allRooms) {
                                // Ignore case (e.g. "Ward A" matches "ward a")
                                if (room.getStationName().equalsIgnoreCase(targetStation)) {
                                    myStationRooms.add(room);
                                }
                            }
                            // ----------------------------

                            runOnUiThread(() -> {
                                layoutLogin.setVisibility(View.GONE);
                                layoutGrid.setVisibility(View.VISIBLE);

                                // Only pass the filtered rooms to the UI and Audio Engine
                                roomAdapter.updateRooms(myStationRooms);
                                audioManager.evaluateAndPlay(myStationRooms);
                            });
                        }
                    } catch (Exception e) {
                        Log.e("CLIENT_TEST", "Error parsing message", e);
                    }
                }
            });

            wsClient.connect();

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
        if (audioManager != null) {
            audioManager.stopAudio();
        }
    }
}