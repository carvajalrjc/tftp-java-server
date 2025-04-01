# 📡 TFTP Java Server and Client

This repository contains the implementation of a **TFTP (Trivial File Transfer Protocol) server and client** written in Java. The project was developed as part of a university assignment for the course **"Communications and Networks"** at **Pontificia Universidad Javeriana**.

---

## 🧠 Project Overview

This project implements the TFTP protocol as described in **RFC 1350**, supporting the transfer of files over **UDP** in both directions:

- **Read Request (RRQ):** Server to Client
- **Write Request (WRQ):** Client to Server

It includes the basic TFTP packet types:
- `RRQ` (1)
- `WRQ` (2)
- `DATA` (3)
- `ACK` (4)
- `ERROR` (5)

---

## 🛠️ Requirements

- Java 11 or higher
- A terminal (Linux, macOS, or Windows)
- Superuser access if using port `69`

Optional:
- Git installed (to clone the project)
- `tftp` client installed for external testing

---

## 🗂️ Project Structure

```bash
.
├── TFTPServer.java       # Main server class (UDP socket on port 69)
├── TFTPHandler.java      # Handles individual RRQ/WRQ requests
├── TFTPClient.java       # Simple command-line client
├── files/                # Server file directory (downloaded/uploaded files)
└── descargas/            # Client directory for downloaded files

```

---

## 🚀 How to Use

### 1. Compile the code
```bash
javac TFTPServer.java TFTPHandler.java TFTPClient.java
```

### 2. Start the server
```bash
java TFTPServer
```

### 3. Start the clienr
```bash
java TFTPClient
```
---

## 🧪 Testing & Functionality

- Download file from server (RRQ)
- Upload file to server (WRQ)
- Error handling (file not found, file already exists)
- Sequential multi-client support

---

### 📚 Credits

- **Proyect developed by**: Juan Camilo Carvajal Rodríguez
- **Course**: Communications and Networks 
- **University**: Pontificia Universidad Javeriana  
- **Professor**: Edgar Ruiz Garcia
