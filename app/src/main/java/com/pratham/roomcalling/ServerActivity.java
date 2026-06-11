package com.pratham.roomcalling;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.pratham.roomcalling.http.DashboardServer;
import java.io.IOException;

public class ServerActivity extends AppCompatActivity {

    private DashboardServer dashboardServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        dashboardServer = new DashboardServer();
        try {
            dashboardServer.start();
            Log.d("HTTP_TEST", "Server started on port 8080");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("HTTP_TEST", "Could not start server", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dashboardServer != null) {
            dashboardServer.stop();
            Log.d("HTTP_TEST", "Server stopped");
        }
    }
}