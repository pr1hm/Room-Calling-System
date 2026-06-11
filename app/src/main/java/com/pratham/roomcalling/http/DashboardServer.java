package com.pratham.roomcalling.http;

import fi.iki.elonen.NanoHTTPD;

public class DashboardServer extends NanoHTTPD {

    // The SRS specifies the server must run on port 8080
    public DashboardServer() {
        super(8080);
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();

        // Route: http://<server-ip>:8080/help
        if (uri.equals("/help")) {
            String responseStr = "<html><body><h1>Hello World from Server!</h1><p>The NanoHTTPD server is running.</p></body></html>";
            return newFixedLengthResponse(Response.Status.OK, "text/html", responseStr);
        }

        // Default fallback for any other URL
        return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "404 Not Found");
    }
}