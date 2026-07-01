package com.pratham.roomcalling;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pratham.roomcalling.adapter.RoomAdapter;
import com.pratham.roomcalling.db.DatabaseHelper;
import com.pratham.roomcalling.model.Room;
import com.pratham.roomcalling.service.ServerService;
import com.pratham.roomcalling.util.RoomAudioManager;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

public class ServerActivity extends AppCompatActivity {

    private String serverIp = "";
    private RoomAdapter roomAdapter;
    private DatabaseHelper dbHelper;
    private RoomAudioManager audioManager;

    // Listens for the internal broadcast from the DashboardServer
    private final BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.pratham.roomcalling.REFRESH_UI".equals(intent.getAction())) {
                refreshLocalUi();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        dbHelper = new DatabaseHelper(this);
        audioManager = new RoomAudioManager(this);
        serverIp = getLocalIpAddress();

        TextView tvIpAddress = findViewById(R.id.tvIpAddress);
        tvIpAddress.setText("IP: " + serverIp);

        Button btnOpenDashboard = findViewById(R.id.btnOpenDashboard);
        Button btnStopServer = findViewById(R.id.btnStopServer);
        RecyclerView recyclerViewRooms = findViewById(R.id.recyclerViewRooms);

        recyclerViewRooms.setLayoutManager(new GridLayoutManager(this, 2));
        roomAdapter = new RoomAdapter();
        recyclerViewRooms.setAdapter(roomAdapter);

        // Start the background Server Service
        Intent serviceIntent = new Intent(this, ServerService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }

        btnOpenDashboard.setOnClickListener(v -> {
            if (serverIp.equals("127.0.0.1") || serverIp.isEmpty()) {
                Toast.makeText(this, "Please connect to Wi-Fi first!", Toast.LENGTH_LONG).show();
            } else {
                String dashboardUrl = "http://" + serverIp + ":8080/";
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(dashboardUrl));
                startActivity(browserIntent);
            }
        });

        btnStopServer.setOnClickListener(v -> shutDownAndExit());
    }

    // Fetches the latest database state and updates the UI and Audio Engine
    private void refreshLocalUi() {
        if (dbHelper != null && roomAdapter != null) {
            // Read settings
            int columns = Integer.parseInt(dbHelper.getSetting("columns", "2"));
            boolean soundEnabled = dbHelper.getSetting("sound_enabled", "1").equals("1");
            int soundRepeats = Integer.parseInt(dbHelper.getSetting("sound_repeats", "1"));

            // Apply columns
            RecyclerView recyclerViewRooms = findViewById(R.id.recyclerViewRooms);
            ((GridLayoutManager) recyclerViewRooms.getLayoutManager()).setSpanCount(columns);

            List<Room> allRooms = dbHelper.getAllRooms();
            roomAdapter.updateRooms(allRooms);

            // Sync settings and play audio
            if (audioManager != null) {
                audioManager.updateSettings(soundEnabled, soundRepeats);
                audioManager.evaluateAndPlay(allRooms);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register the BroadcastReceiver with Android 13+ safety checks
        IntentFilter filter = new IntentFilter("com.pratham.roomcalling.REFRESH_UI");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(updateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(updateReceiver, filter);
        }

        // Force an immediate UI refresh when returning to the app
        refreshLocalUi();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Prevent memory leaks by unregistering when the app is backgrounded
        try {
            unregisterReceiver(updateReceiver);
        } catch (IllegalArgumentException e) {
            // Receiver was not registered
        }
    }

    private void shutDownAndExit() {
        Toast.makeText(this, "Shutting down Server...", Toast.LENGTH_SHORT).show();
        if (audioManager != null) audioManager.stopAudio();
        Intent serviceIntent = new Intent(this, ServerService.class);
        stopService(serviceIntent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (audioManager != null) audioManager.stopAudio();
    }

    private String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return "127.0.0.1";
    }
}