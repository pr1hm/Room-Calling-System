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
import java.util.Collections;
import java.util.List;

public class MasterActivity extends AppCompatActivity {

    private RoomWebSocketClient wsClient;
    private EditText etServerIp;
    private LinearLayout layoutLogin, layoutGrid;
    private RecyclerView recyclerViewRooms;
    private RoomAdapter roomAdapter;
    private RoomAudioManager audioManager;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_master);

        gson = new Gson();
        audioManager = new RoomAudioManager(this);

        // UI References
        etServerIp = findViewById(R.id.etServerIp);
        Button btnConnect = findViewById(R.id.btnConnect);
        layoutLogin = findViewById(R.id.layoutLogin);
        layoutGrid = findViewById(R.id.layoutGrid);
        recyclerViewRooms = findViewById(R.id.recyclerViewRooms);

        // Setup Grid (Using 2 columns, reusing the Client's Adapter)
        recyclerViewRooms.setLayoutManager(new GridLayoutManager(this, 2));
        roomAdapter = new RoomAdapter();
        recyclerViewRooms.setAdapter(roomAdapter);

        btnConnect.setOnClickListener(v -> connectToServer());
    }

    private void connectToServer() {
        String ipAddress = etServerIp.getText().toString().trim();

        if (ipAddress.isEmpty()) {
            Toast.makeText(this, "Please enter the Server IP", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            URI uri = new URI("ws://" + ipAddress + ":8090");
            wsClient = new RoomWebSocketClient(uri);

            wsClient.setWebSocketListener(new RoomWebSocketClient.WebSocketListener() {
                @Override
                public void onConnected() {
                    runOnUiThread(() -> Toast.makeText(MasterActivity.this, "Supervisor Connected!", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onDisconnected(String reason) {
                    runOnUiThread(() -> Toast.makeText(MasterActivity.this, "Connection Lost: " + reason, Toast.LENGTH_LONG).show());
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

                            // --- SUPERVISOR FEATURE: Sort by Emergency Status (Highest First) ---
                            Collections.sort(allRooms, (r1, r2) -> Integer.compare(r2.getStatus(), r1.getStatus()));
                            // --------------------------------------------------------------------

                            runOnUiThread(() -> {
                                layoutLogin.setVisibility(View.GONE);
                                layoutGrid.setVisibility(View.VISIBLE);

                                // Show ALL rooms, sorted by priority
                                roomAdapter.updateRooms(allRooms);

                                // Play audio if there's an emergency anywhere in the building
                                audioManager.evaluateAndPlay(allRooms);
                            });
                        }
                    } catch (Exception e) {
                        Log.e("MASTER_TEST", "Error parsing message", e);
                    }
                }
            });

            wsClient.connect();

        } catch (Exception e) {
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