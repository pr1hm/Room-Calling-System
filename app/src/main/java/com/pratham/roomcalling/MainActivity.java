package com.pratham.roomcalling;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "RoomCallingPrefs";
    private static final String KEY_MODE = "DeviceMode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if a mode was already selected previously
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedMode = prefs.getString(KEY_MODE, null);

        if (savedMode != null) {
            launchMode(savedMode);
            return; // Skip loading the UI entirely
        }

        setContentView(R.layout.activity_main);

        // Bind to the NEW IDs from the updated activity_main.xml
        Button btnModeServer = findViewById(R.id.btnModeServer);
        Button btnModeClient = findViewById(R.id.btnModeClient);
        Button btnModeMaster = findViewById(R.id.btnModeMaster);

        btnModeServer.setOnClickListener(v -> saveAndLaunch("SERVER"));
        btnModeClient.setOnClickListener(v -> saveAndLaunch("CLIENT"));
        btnModeMaster.setOnClickListener(v -> saveAndLaunch("MASTER"));
    }

    private void saveAndLaunch(String mode) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putString(KEY_MODE, mode).apply();
        launchMode(mode);
    }

    private void launchMode(String mode) {
        Intent intent;
        switch (mode) {
            case "SERVER":
                intent = new Intent(this, ServerActivity.class);
                break;
            case "MASTER":
                intent = new Intent(this, MasterActivity.class);
                break;
            case "CLIENT":
            default:
                intent = new Intent(this, ClientActivity.class);
                break;
        }
        startActivity(intent);
        finish(); // Close MainActivity so the user can't use the back button to return here
    }
}