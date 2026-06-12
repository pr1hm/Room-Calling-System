package com.pratham.roomcalling.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.pratham.roomcalling.db.DatabaseHelper;
import com.pratham.roomcalling.http.DashboardServer;
import com.pratham.roomcalling.websocket.RoomWebSocketServer;

public class ServerService extends Service {

    private DashboardServer dashboardServer;
    private RoomWebSocketServer webSocketServer;
    private PowerManager.WakeLock wakeLock;

    @Override
    public void onCreate() {
        super.onCreate();

        // 1. Grab a WakeLock to keep the CPU running when screen is off
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "RoomCalling::ServerWakeLock");
        wakeLock.acquire();

        // 2. Start the Foreground Notification (Required by Android)
        startForeground(1, createNotification());

        // 3. Initialize Database and Servers (Moved from ServerActivity)
        DatabaseHelper db = new DatabaseHelper(this);

        try {
            webSocketServer = new RoomWebSocketServer(db);
            webSocketServer.setReuseAddr(true);
            webSocketServer.start();
            Log.d("SERVER_SERVICE", "WebSocket started on 8090");

            dashboardServer = new DashboardServer(this, webSocketServer);
            dashboardServer.start();
            Log.d("SERVER_SERVICE", "HTTP Dashboard started on 8080");

        } catch (Exception e) {
            Log.e("SERVER_SERVICE", "Failed to start servers", e);
        }
    }

    private Notification createNotification() {
        String channelId = "ServerChannel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId, "Room Server Service", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }

        return new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Room Calling Server Running")
                .setContentText("Servers are active in the background.")
                // Using a built-in Android icon to save time, you can replace this later
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setOngoing(true)
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // If Android kills the service for memory, restart it immediately
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("SERVER_SERVICE", "Shutting down servers...");

        if (dashboardServer != null) dashboardServer.stop();
        if (webSocketServer != null) {
            try { webSocketServer.stop(); } catch (InterruptedException ignored) {}
        }

        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // We don't need to bind to this service
    }
}