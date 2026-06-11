package com.pratham.roomcalling;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "RoomCallingPrefs";
    private static final String KEY_ROLE = "DeviceRole";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Check if a role is already saved
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedRole = prefs.getString(KEY_ROLE, null);

        if (savedRole != null) {
            // Role exists, launch directly into that Activity
            launchRoleActivity(savedRole);
            return; // Stop running the rest of onCreate
        }

        // 2. If no role is saved, show the selection screen
        setContentView(R.layout.activity_main);

        Button btnServer = findViewById(R.id.btnServer);
        Button btnClient = findViewById(R.id.btnClient);
        Button btnMaster = findViewById(R.id.btnMaster);

        btnServer.setOnClickListener(v -> saveRoleAndLaunch("SERVER"));
        btnClient.setOnClickListener(v -> saveRoleAndLaunch("CLIENT"));
        btnMaster.setOnClickListener(v -> saveRoleAndLaunch("MASTER"));
    }

    private void saveRoleAndLaunch(String role) {
        // Save the choice
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putString(KEY_ROLE, role).apply();

        // Launch the activity
        launchRoleActivity(role);
    }

    private void launchRoleActivity(String role) {
        Intent intent;
        switch (role) {
            case "SERVER":
                intent = new Intent(this, ServerActivity.class);
                break;
            case "CLIENT":
                intent = new Intent(this, ClientActivity.class);
                break;
            case "MASTER":
                intent = new Intent(this, MasterActivity.class);
                break;
            default:
                return;
        }
        startActivity(intent);
        finish(); // Close MainActivity so the user can't press 'Back' to return to it
    }
}