package com.pratham.roomcalling.http;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.util.Log;
import com.google.gson.Gson;
import com.pratham.roomcalling.db.DatabaseHelper;
import com.pratham.roomcalling.model.Room;
import com.pratham.roomcalling.websocket.RoomWebSocketServer;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import fi.iki.elonen.NanoHTTPD;

public class DashboardServer extends NanoHTTPD {

    private final DatabaseHelper dbHelper;
    private final Gson gson;
    private final RoomWebSocketServer wsServer;
    private final String serverIp;
    private final Context context;

    public DashboardServer(Context context, RoomWebSocketServer wsServer) {
        super(8080);
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
        this.gson = new Gson();
        this.wsServer = wsServer;
        this.serverIp = getLocalIpAddress();
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        Map<String, String> parms = session.getParms();

        if (uri != null && uri.contains("/report")) {
            Response reportResponse = newFixedLengthResponse(Response.Status.OK, "text/html", buildReportDashboard());
            reportResponse.addHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            return reportResponse;
        }

        String action = parms.get("a");

        if (action != null && !action.isEmpty()) {
            try {
                char command = action.charAt(0);
                String data = action.length() > 1 ? action.substring(1) : "";

                switch (command) {
                    case 'r': // Add/Update Room: r101,Name
                        String[] rParts = data.split(",");
                        if (rParts.length == 2) {
                            dbHelper.addOrUpdateRoom(rParts[0], rParts[1]);
                            broadcastUpdate();
                            return newFixedLengthResponse(Response.Status.OK, "text/plain", "Success: Room Saved");
                        }
                        break;
                    case 'x': // Delete Room: x101
                        dbHelper.deleteRoom(data);
                        broadcastUpdate();
                        return newFixedLengthResponse(Response.Status.OK, "text/plain", "Success: Room Deleted");
                    case 'k': // Status Change: k101,4
                        String[] kParts = data.split(",");
                        if (kParts.length == 2) {
                            dbHelper.updateStatus(kParts[0], Integer.parseInt(kParts[1]));
                            broadcastUpdate();
                            return newFixedLengthResponse(Response.Status.OK, "text/plain", "Success: Status Updated");
                        }
                        break;
                    case 'a': // Sound Toggle: a0 or a1
                        dbHelper.saveSetting("sound_enabled", data);
                        broadcastUpdate();
                        return newFixedLengthResponse(Response.Status.OK, "text/plain", "Success: Sound Toggled");
                    case 'c': // Columns: c3
                        dbHelper.saveSetting("columns", data);
                        broadcastUpdate();
                        return newFixedLengthResponse(Response.Status.OK, "text/plain", "Success: Columns Updated");
                    case 'n': // Repeats: n0
                        dbHelper.saveSetting("sound_repeats", data);
                        broadcastUpdate();
                        return newFixedLengthResponse(Response.Status.OK, "text/plain", "Success: Repeats Updated");
                    case 'p': // Priority: p101,102
                        dbHelper.setPriorityList(data.split(","));
                        broadcastUpdate();
                        return newFixedLengthResponse(Response.Status.OK, "text/plain", "Success: Priorities Updated");
                    case 'g': // Add Group: g5:101,102
                        String[] gParts = data.split(":");
                        if (gParts.length == 2) {
                            dbHelper.setRoomGroup(gParts[0], gParts[1].split(","));
                            broadcastUpdate();
                            return newFixedLengthResponse(Response.Status.OK, "text/plain", "Success: Group Configured");
                        }
                        break;
                    case 'd': // Delete Group: d5
                        dbHelper.deleteRoomGroup(data);
                        broadcastUpdate();
                        return newFixedLengthResponse(Response.Status.OK, "text/plain", "Success: Group Deleted");
                    case 't': // View Room Groups
                        return newFixedLengthResponse(Response.Status.OK, "text/plain", dbHelper.getRoomGroupListText());
                }
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "Error: Invalid Format");
            } catch (Exception e) {
                Log.e("HTTP_SERVER", "API Error", e);
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Error: " + e.getMessage());
            }
        }

        Response response = newFixedLengthResponse(Response.Status.OK, "text/html", buildHelpDashboard());
        response.addHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        return response;
    }

    private void broadcastUpdate() {
        if (wsServer != null) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("action", "REFRESH_ALL");
            payload.put("data", dbHelper.getAllRooms());
            payload.put("columns", dbHelper.getSetting("columns", "2"));
            payload.put("sound_enabled", dbHelper.getSetting("sound_enabled", "1"));
            payload.put("sound_repeats", dbHelper.getSetting("sound_repeats", "1"));
            wsServer.broadcastUpdate(gson.toJson(payload));
        }
        context.sendBroadcast(new Intent("com.pratham.roomcalling.REFRESH_UI"));
    }

    private String buildHelpDashboard() {
        String base = "http://" + serverIp + ":8080";

        // Fetch System Diagnostics for Server Details Table
        String dateStr = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
        String deviceName = android.os.Build.MODEL;
        String osVersion = "Android " + android.os.Build.VERSION.RELEASE;

        ActivityManager actManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        actManager.getMemoryInfo(memInfo);
        String ramStr = String.format(Locale.getDefault(), "%.2f GB", memInfo.totalMem / (1024.0 * 1024.0 * 1024.0));

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        String resolution = metrics.widthPixels + "x" + metrics.heightPixels;

        return "<html><body style='font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #ffffff;'>" +

                // 1. Server Details Section
                "<div style='max-width: 900px; margin: auto;'>" +
                "<table border='1' cellpadding='8' style='border-collapse: collapse; width: 100%; border: 1px solid #E2E8F0; font-size: 14px;'>" +
                "<tr style='background-color: #6366F1; color: white;'><th colspan='2' style='text-align: left;'>Server Details</th></tr>" +
                "<tr><td width='30%'>Date</td><td>" + dateStr + "</td></tr>" +
                "<tr style='background-color: #F8FAFC;'><td>Device Name</td><td>" + deviceName + "</td></tr>" +
                "<tr><td>OS</td><td>" + osVersion + "</td></tr>" +
                "<tr style='background-color: #F8FAFC;'><td>RAM</td><td>" + ramStr + "</td></tr>" +
                "<tr><td>Resolution</td><td>" + resolution + "</td></tr>" +
                "<tr style='background-color: #F8FAFC;'><td>IP Address</td><td>" + serverIp + "</td></tr>" +
                "<tr><td>Version</td><td>1.1.0</td></tr>" +
                "</table><br><br>" +

                // 2. API IoT Reference Section
                "<h2>How to use this application?</h2>" +
                "<table border='1' cellpadding='8' style='border-collapse: collapse; width: 100%; border: 1px solid #E2E8F0; font-size: 14px;'>" +
                "<tr style='background-color: #6366F1; color: white;'><th width='30%' style='text-align: left;'>Title</th><th style='text-align: left;'>Browser</th></tr>" +
                "<tr><td>Web Server Port</td><td>8080</td></tr>" +
                "<tr style='background-color: #F8FAFC;'><td>Socket Server Port</td><td>8090</td></tr>" +

                "<tr><td>Add / Update Room</td><td><a href='" + base + "/?a=r101,JohnDoe' style='color: #4F46E5; text-decoration: none;'>" + base + "/?a=r&lt;roomId&gt;,&lt;roomName&gt;</a></td></tr>" +
                "<tr style='background-color: #F8FAFC;'><td>Delete Room</td><td><a href='" + base + "/?a=x101' style='color: #4F46E5; text-decoration: none;'>" + base + "/?a=x&lt;roomId&gt;</a></td></tr>" +
                "<tr><td>Status Change</td><td><a href='" + base + "/?a=k101,4' style='color: #4F46E5; text-decoration: none;'>" + base + "/?a=k&lt;roomId&gt;,&lt;status&gt;</a></td></tr>" +

                "<tr style='background-color: #F8FAFC;'><td>Sound OFF/ON</td><td><a href='" + base + "/?a=a1' style='color: #4F46E5; text-decoration: none;'>" + base + "/?a=a0/1</a></td></tr>" +
                "<tr><td>Number of Columns</td><td><a href='" + base + "/?a=c3' style='color: #4F46E5; text-decoration: none;'>" + base + "/?a=c3</a></td></tr>" +
                "<tr style='background-color: #F8FAFC;'><td>Sound Repeat Time</td><td><a href='" + base + "/?a=n10' style='color: #4F46E5; text-decoration: none;'>" + base + "/?a=n10</a><br><small style='color:#64748B;'>Set to 0 if do not want it to repeat</small></td></tr>" +

                "<tr><td>Add Room Group</td><td><a href='" + base + "/?a=g5:101,102' style='color: #4F46E5; text-decoration: none;'>" + base + "/?a=g5:1,2,3</a></td></tr>" +
                "<tr style='background-color: #F8FAFC;'><td>Delete Room Group</td><td><a href='" + base + "/?a=d5' style='color: #4F46E5; text-decoration: none;'>" + base + "/?a=d1</a></td></tr>" +
                "<tr><td>Add Priority list</td><td><a href='" + base + "/?a=p101,102' style='color: #4F46E5; text-decoration: none;'>" + base + "/?a=p1,2,3</a></td></tr>" +
                "<tr style='background-color: #F8FAFC;'><td>Room Group List</td><td><a href='" + base + "/?a=t' style='color: #4F46E5; text-decoration: none;'>" + base + "/?a=t</a></td></tr>" +

                "<tr><td>Download Report</td><td><a href='" + base + "/report' style='color: #4F46E5; text-decoration: none;'>" + base + "/report</a></td></tr>" +
                "</table></div>" +
                "</body></html>";
    }

    private String buildReportDashboard() {
        List<String[]> logs = dbHelper.getCallLogs();
        StringBuilder tableRows = new StringBuilder();
        for (String[] log : logs) {
            String statusText = log[1].equals("4") ? "Code Blue" : log[1].equals("3") ? "Care Required" : log[1].equals("2") ? "Assistance" : "Standard";
            tableRows.append("<tr><td>").append(log[2]).append("</td><td>").append(log[0]).append("</td><td>").append(statusText).append("</td></tr>");
        }
        return "<html><body style='font-family: Arial; padding: 20px;'><button onclick='window.print()'>Download PDF</button><table border='1' cellpadding='10' style='border-collapse: collapse; width: 100%; margin-top:20px;'><tr style='background-color: #E2E8F0;'><th>Timestamp</th><th>Room ID</th><th>Alert Type</th></tr>" + tableRows.toString() + "</table></body></html>";
    }

    private String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) return inetAddress.getHostAddress();
                }
            }
        } catch (Exception ex) { }
        return "127.0.0.1";
    }
}