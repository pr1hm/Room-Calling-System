package com.pratham.roomcalling.http;

import android.content.Context;
import android.util.Log;
import com.google.gson.Gson;
import com.pratham.roomcalling.db.DatabaseHelper;
import com.pratham.roomcalling.model.Room;
import com.pratham.roomcalling.websocket.RoomWebSocketServer;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import fi.iki.elonen.NanoHTTPD;

public class DashboardServer extends NanoHTTPD {

    private final Context context;
    private final DatabaseHelper dbHelper;
    private final Gson gson;
    private final RoomWebSocketServer wsServer;

    public DashboardServer(Context context, RoomWebSocketServer wsServer) {
        super(8080);
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
        this.gson = new Gson();
        this.wsServer = wsServer;
    }

    @Override
    public Response serve(IHTTPSession session) {
        Method method = session.getMethod();
        String uri = session.getUri();

        // 1. Serve the Patient Room Page
        if (Method.GET.equals(method) && uri.startsWith("/room/") && !uri.equals("/room/add")) {
            try {
                String requestedRoomId = uri.substring("/room/".length());
                InputStream is = context.getAssets().open("room_page.html");
                java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
                String htmlTemplate = s.hasNext() ? s.next() : "";
                is.close();
                String finalHtml = htmlTemplate.replace("{{ROOM_ID}}", requestedRoomId);
                return newFixedLengthResponse(Response.Status.OK, "text/html", finalHtml);
            } catch (Exception e) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "Could not load page.");
            }
        }

        // 2. Handle Status Updates from the Patient Page
        if (Method.POST.equals(method) && uri.equals("/update")) {
            try {
                Integer contentLength = Integer.parseInt(session.getHeaders().get("content-length"));
                byte[] buffer = new byte[contentLength];
                session.getInputStream().read(buffer, 0, contentLength);
                String jsonBody = new String(buffer);

                Map<String, Object> requestData = gson.fromJson(jsonBody, Map.class);
                String roomId = (String) requestData.get("roomId");
                int status = ((Double) requestData.get("status")).intValue();

                boolean success = dbHelper.updateStatus(roomId, status);

                if (success) {
                    Log.d("HTTP_TEST", "Room " + roomId + " updated to status " + status);
                    List<Room> allRooms = dbHelper.getAllRooms();
                    Map<String, Object> payload = new HashMap<>();
                    payload.put("action", "REFRESH_ALL");
                    payload.put("station", "Ward A");
                    payload.put("data", allRooms);

                    if (wsServer != null) {
                        wsServer.broadcastUpdate(gson.toJson(payload));
                    }
                    return newFixedLengthResponse(Response.Status.OK, "application/json", "{\"message\":\"Success\"}");
                } else {
                    Log.e("HTTP_TEST", "Failed! Room " + roomId + " not found.");
                    return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json", "{\"message\":\"Failed to update\"}");
                }

            } catch (Exception e) {
                Log.e("HTTP_TEST", "Error processing update", e);
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json", "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }

        // 3. Serve the Admin Dashboard on /help
        if (Method.GET.equals(method) && uri.equals("/help")) {
            try {
                InputStream is = context.getAssets().open("dashboard.html");
                return newChunkedResponse(Response.Status.OK, "text/html", is);
            } catch (Exception e) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "Could not load dashboard.");
            }
        }

        // 4. Handle Adding a New Room
        if (Method.POST.equals(method) && uri.equals("/room/add")) {
            try {
                Integer contentLength = Integer.parseInt(session.getHeaders().get("content-length"));
                byte[] buffer = new byte[contentLength];
                session.getInputStream().read(buffer, 0, contentLength);
                String jsonBody = new String(buffer);

                Map<String, String> requestData = gson.fromJson(jsonBody, Map.class);
                String newRoomId = requestData.get("roomId");
                String newRoomName = requestData.get("roomName");
                String newStationName = requestData.get("stationName");

                boolean success = dbHelper.addRoom(newRoomId, newRoomName, newStationName);

                if (success) {
                    Log.d("HTTP_TEST", "Successfully added new room: " + newRoomId);
                    List<Room> allRooms = dbHelper.getAllRooms();
                    Map<String, Object> payload = new HashMap<>();
                    payload.put("action", "REFRESH_ALL");
                    payload.put("station", "Ward A");
                    payload.put("data", allRooms);

                    if (wsServer != null) {
                        wsServer.broadcastUpdate(gson.toJson(payload));
                    }
                    return newFixedLengthResponse(Response.Status.OK, "application/json", "{\"message\":\"Success\"}");
                } else {
                    return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json", "{\"message\":\"Failed\"}");
                }

            } catch (Exception e) {
                Log.e("HTTP_TEST", "Error adding room", e);
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json", "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }

        return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "404 Not Found");
    }
}