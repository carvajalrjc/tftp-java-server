# 📡 Servidor y Cliente TFTP en Java

Este repositorio contiene la implementación de un **servidor y cliente TFTP (Trivial File Transfer Protocol)** desarrollado en Java. El proyecto fue realizado como parte de una asignación académica para la materia **"Comunicaciones y Redes"** en la **Pontificia Universidad Javeriana**.

---

## 🧠 Descripción del Proyecto

Este proyecto implementa el protocolo TFTP según lo descrito en el **RFC 1350**, permitiendo la transferencia de archivos sobre **UDP** en ambas direcciones:

- **Solicitud de Lectura (RRQ):** Servidor a Cliente
- **Solicitud de Escritura (WRQ):** Cliente a Servidor

Incluye los tipos de paquetes TFTP definidos por el estándar:

- `RRQ` (1)
- `WRQ` (2)
- `DATA` (3)
- `ACK` (4)
- `ERROR` (5)

---

## 🛠️ Requisitos

- Java 11 o superior
- Una terminal (Linux, macOS o Windows)
- Acceso como superusuario si se utiliza el puerto `69`

Opcional:
- Git instalado (para clonar el proyecto)
- Cliente `tftp` para pruebas externas

---

## 🗂️ Estructura del Proyecto

```bash
.
├── TFTPServer.java       # Clase principal del servidor (socket UDP en el puerto 69)
├── TFTPHandler.java      # Maneja solicitudes RRQ/WRQ individuales
├── TFTPClient.java       # Cliente por línea de comandos
├── files/                # Carpeta del servidor para archivos (subidos/descargados)
└── descargas/            # Carpeta del cliente para archivos descargados
```

---

## 🚀 Cómo Ejecutarlo

### 1. Compilar el código
```bash
javac TFTPServer.java TFTPHandler.java TFTPClient.java
```

### 2. Iniciar el servidor
```bash
java TFTPServer
```

### 3. Iniciar el cliente
```bash
java TFTPClient
```
---

## 🧪 Pruebas y Funcionalidad
- Descarga de archivos desde el servidor (RRQ)
- Subida de archivos al servidor (WRQ)
- Manejo de errores (archivo no encontrado, archivo ya existente)
- Soporte para múltiples clientes de manera secuencial
---

### 📚 Créditos

-  Proyecto desarrollado por: Juan Camilo Carvajal Rodríguez
-  Materia: Comunicaciones y Redes
-  Universidad: Pontificia Universidad Javeriana
-  Profesor: Edgar Ruiz García
