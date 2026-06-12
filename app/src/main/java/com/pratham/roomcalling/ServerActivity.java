package com.pratham.roomcalling;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.pratham.roomcalling.service.ServerService;

public class ServerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        // UI Kill Switch
        Button btnStopServer = findViewById(R.id.btnStopServer);
        btnStopServer.setOnClickListener(v -> shutDownAndExit());

        // Launch the Background Service
        Intent serviceIntent = new Intent(this, ServerService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    private void shutDownAndExit() {
        Toast.makeText(this, "Shutting down Background Service...", Toast.LENGTH_SHORT).show();

        // Stop the service
        Intent serviceIntent = new Intent(this, ServerService.class);
        stopService(serviceIntent);

        finish();
    }
}