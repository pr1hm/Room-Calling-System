# Room Calling System - Distributed Local Healthcare Network

![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)
![Language](https://img.shields.io/badge/Language-Java-007396?logo=java&logoColor=white)
![Database](https://img.shields.io/badge/Database-SQLite-003B57?logo=sqlite&logoColor=white)
![Network](https://img.shields.io/badge/Network-Local_WLAN-blue)

A decentralized, real-time alert and monitoring platform designed for hospitals, care centers, and wards. Built natively in Java for Android devices as part of an internship project at **Netsol IT Solutions Pvt Ltd**.

This system operates entirely on a Local Area Network (WLAN) without requiring internet access or cloud servers, ensuring maximum privacy, zero external latency, and complete operational independence.

---

## 🏗️ Architecture & Topology

The system uses a Single Source of Truth (SSOT) architecture, divided into distinct role-based nodes. 

### 1. Server Node (The Brain)
* **Role:** The central hub of the system. 
* **Function:** Hosts the localized SQLite database. Runs a background HTTP Server (NanoHTTPD) on Port 8080 to serve web pages, and a WebSocket Server on Port 8090 to broadcast real-time data changes.
* **Resilience:** Encapsulated in an Android `Foreground Service` utilizing `PowerManager.WakeLock` to prevent the OS from killing the network when the device screen sleeps.

### 2. Client Node (Nurse Station)
* **Role:** Local ward monitoring.
* **Function:** Connects to the Server via WebSockets. Filters all incoming network traffic so the Nurse only sees patients in their explicitly assigned Ward/Station.
* **Audio Engine:** Automatically maps incoming alerts to Android system audio channels (standard Notification chimes for assistance, aggressive Alarm sirens for emergencies).

### 3. Master Node (Supervisor Console)
* **Role:** Global facility monitoring.
* **Function:** Bypasses station filters to display every active room in the hospital. Utilizes a real-time sorting algorithm (`Collections.sort`) to instantly snap any active emergencies to the top of the grid.

### 4. Patient Node (Web Portal)
* **Role:** The patient interface.
* **Function:** Device-agnostic. Any smartphone, tablet, or PC connected to the Wi-Fi network can act as a patient node by simply opening a web browser and navigating to `http://<SERVER_IP>:8080/room/<ID>`.

---

## ✨ Core Functionalities

* **Zero-Latency WebSockets:** Database updates trigger an instant `REFRESH_ALL` JSON broadcast to all connected Android devices, updating UI grids in milliseconds.
* **Dynamic URL Routing:** The HTTP server parses incoming URLs and injects room data directly into HTML templates. It includes secure 404 Error handling to block access to non-existent rooms.
* **Alarm Fatigue Mitigation:** Client Nodes strictly filter out data from other wards, ensuring nurses are not distracted by alerts outside their jurisdiction.
* **Admin Dashboard:** Accessible via `http://<SERVER_IP>:8080/help`. Allows IT or administrative staff to dynamically Admit (create) or Checkout (delete) rooms without altering code.
* **Persistent Role Selection:** Android `SharedPreferences` saves the initial setup configuration (Server, Client, or Master) to streamline device reboots.
* **Premium UI/UX:** Features a unified "Slate & White" modern dashboard design across all Android screens, and Tailwind CSS for the web portals.

---

## 🛠️ Technical Stack

* **Platform:** Android (API Level 23+)
* **Programming Language:** Java
* **Local Database:** SQLite (`SQLiteOpenHelper`)
* **HTTP Server:** [NanoHTTPD](https://github.com/NanoHttpd/nanohttpd)
* **WebSocket Server/Client:** [Java-WebSocket](https://github.com/TooTallNate/Java-WebSocket)
* **JSON Serialization:** Google Gson
* **Frontend (Web):** HTML5, JavaScript (Fetch API), Tailwind CSS (CDN)
* **Android UI:** Native XML (`RecyclerView`, `CardView`)

---

## 🚀 Installation & Deployment Guide

### Prerequisites
1.  All devices (Androids, PCs, Patient tablets) **must** be connected to the exact same Wi-Fi router/network.
2.  Obtain the generated `.apk` file.

### Step 1: Deploying the Server
1.  Install the APK on the device designated as the central Server (can be a standard Android phone or tablet).
2.  Launch the app and select **SERVER NODE**.
3.  Note the IP address displayed on the screen (e.g., `192.168.1.100`).
4.  You may turn off the screen; the Foreground Service will keep the server alive.

### Step 2: Setting up the Facility (Admin)
1.  Open a web browser on any PC or tablet connected to the network.
2.  Navigate to `http://<SERVER_IP>:8080/help`.
3.  Use the **Admin Dashboard** to Admit new rooms (e.g., Room 101 in "Ward A", Room 205 in "ICU").

### Step 3: Deploying Nurse Stations (Clients)
1.  Install the APK on a tablet at the nurse's desk.
2.  Launch the app and select **NURSE STATION**.
3.  Enter the Server IP.
4.  Enter the specific Ward Name (e.g., `Ward A`) exactly as you typed it in the Admin Dashboard.
5.  Click Connect. The grid will now monitor only that specific ward.

### Step 4: Deploying the Supervisor Console (Master)
1.  Install the APK on the Head Nurse's or Administrator's device.
2.  Launch the app and select **SUPERVISOR MASTER**.
3.  Enter the Server IP and connect. The grid will monitor the entire facility.

### Step 5: Connecting Patients
1.  On the patient's bedside device (iPad, cheap Android tablet, or smartphone), open the web browser.
2.  Navigate to `http://<SERVER_IP>:8080/room/<ROOM_ID>` (e.g., `http://192.168.1.100:8080/room/101`).
3.  Lock the device to this screen. The patient can now tap buttons to trigger instant alerts across the network.

---

## 🔒 Security & Scope Constraints
* **Port Availability:** The system utilizes Ports `8080` and `8090`. Ensure the network router does not restrict inter-device traffic on these ports.
* **Network Isolation:** This system has no authentication middleware, as it relies on Physical Network Isolation. Only devices granted access to the local Wi-Fi password can interact with the server.

---
*Developed by S. Pratham during the Netsol IT Solutions Pvt Ltd Internship.*
