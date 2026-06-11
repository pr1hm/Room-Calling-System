package com.pratham.roomcalling;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        com.pratham.roomcalling.db.DatabaseHelper db = new com.pratham.roomcalling.db.DatabaseHelper(this);
        db.addRoom("101", "Room 101 - Test Patient", "Ward A");
        java.util.List<com.pratham.roomcalling.model.Room> rooms = db.getAllRooms();
        android.util.Log.d("DB_TEST", "Rooms in DB: " + rooms.size());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}