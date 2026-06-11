# Room Calling System (Android)

A distributed real-time alert and monitoring platform designed for hospitals, care centers, and wards. Built natively in Java for Android devices as part of an internship project at Netsol IT Solutions Pvt Ltd.

## Architecture
This application uses a Single Source of Truth (SSOT) decentralized architecture running entirely on local area networks (WLAN). No external cloud servers are required.
* **Server Role:** One Android device runs a local SQLite database, hosts a local web dashboard via NanoHTTPD (port 8080), and broadcasts real-time state changes via a WebSocket server (port 8090).
* **Client Role (Nurse Station):** Connects to the Server via WebSockets to display a live grid of patient room statuses.
* **Patient Node:** Patients use a standard web browser on their device to connect to the Server's HTTP dashboard, triggering status updates via REST API calls.

## Tech Stack
* **Language:** Java (Android API 23+)
* **Database:** SQLite (built-in Android `SQLiteOpenHelper`)
* **Local Web Server:** [NanoHTTPD](https://github.com/NanoHttpd/nanohttpd)
* **Real-time Comms:** [Java-WebSocket](https://github.com/TooTallNate/Java-WebSocket)
* **JSON Parsing:** Google Gson
* **UI Components:** RecyclerView, CardView, SharedPreferences

## Features Implemented
* Dynamic Role Selection (Server, Client, Master) saved securely in SharedPreferences.
* Foreground background service architecture to keep servers alive.
* Bi-directional WebSocket communication for sub-second UI updates across the network.
* Embedded HTML/JS patient control panels served directly from the Android `assets` folder.
* Color-coded emergency status grid (Call, Assistance, Care Required, Code Blue).

## Setup & Testing
1. Connect two Android devices (or one emulator and one physical device) to the same Wi-Fi network.
2. Launch the app on Device A and select **SERVER**. Note the IP address.
3. Launch the app on Device B and select **CLIENT**. Enter Device A's IP address to connect.
4. Open a web browser on any device on the network and navigate to `http://<SERVER_IP>:8080/room/101`.
5. Pressing a status button on the web interface will instantly update the Client device's UI via WebSockets.
