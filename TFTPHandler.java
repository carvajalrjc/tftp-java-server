import java.io.*;
import java.net.*;

public class TFTPHandler implements Runnable {
    private final DatagramPacket initialRequest;
    private static final int BLOCK_SIZE = 512;

    public TFTPHandler(DatagramPacket request) {
        this.initialRequest = request;
    }

    @Override
    public void run() {
        try {
            byte[] data = initialRequest.getData();
            int opcode = data[1];
            InetAddress clientAddress = initialRequest.getAddress();
            int clientPort = initialRequest.getPort();

            switch (opcode) {
                case 1: // RRQ
                    handleRRQ(data, clientAddress, clientPort);
                    break;
                case 2: // WRQ
                    handleWRQ(data, clientAddress, clientPort);
                    break;
                default:
                    sendError(4, "Operación no soportada", clientAddress, clientPort);
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String extractFilename(byte[] data) {
        int i = 2;
        StringBuilder filename = new StringBuilder();
        while (data[i] != 0) {
            filename.append((char) data[i++]);
        }
        return filename.toString();
    }

    private void handleRRQ(byte[] requestData, InetAddress address, int port) throws IOException {
        String filename = extractFilename(requestData);
        File file = new File("files/" + filename);

        if (!file.exists()) {
            sendError(1, "Archivo no encontrado", address, port);
            return;
        }

        try (FileInputStream fis = new FileInputStream(file);
            DatagramSocket socket = new DatagramSocket()) {

            int blockNumber = 1;
            byte[] block = new byte[BLOCK_SIZE];
            int bytesRead;

            while ((bytesRead = fis.read(block)) != -1) {
                byte[] dataPacket = createDataPacket(blockNumber, block, bytesRead);
                DatagramPacket sendPacket = new DatagramPacket(dataPacket, dataPacket.length, address, port);
                socket.send(sendPacket);

                // Espera ACK
                byte[] ack = new byte[4];
                DatagramPacket ackPacket = new DatagramPacket(ack, ack.length);
                socket.receive(ackPacket);

                blockNumber++;
                if (bytesRead < BLOCK_SIZE) break; // Último bloque
            }
        }
    }

    private void handleWRQ(byte[] requestData, InetAddress address, int port) throws IOException {
        String filename = extractFilename(requestData);
        File file = new File("files/" + filename);
        if (file.exists()) {
            sendError(6, "Archivo ya existe", address, port);
            return;
        }

        try (FileOutputStream fos = new FileOutputStream(file);
            DatagramSocket socket = new DatagramSocket()) {

            // Enviar ACK con bloque 0
            sendAck(0, socket, address, port);

            int blockExpected = 1;

            while (true) {
                byte[] dataBuffer = new byte[516];
                DatagramPacket dataPacket = new DatagramPacket(dataBuffer, dataBuffer.length);
                socket.receive(dataPacket);

                int opcode = dataBuffer[1];
                if (opcode != 3) {
                    sendError(4, "Esperado paquete de datos", address, port);
                    break;
                }

                int blockNum = ((dataBuffer[2] & 0xff) << 8) | (dataBuffer[3] & 0xff);
                if (blockNum != blockExpected) continue;

                fos.write(dataBuffer, 4, dataPacket.getLength() - 4);
                sendAck(blockNum, socket, address, port);

                if (dataPacket.getLength() < 516) break; // último bloque
                blockExpected++;
            }
        }
    }

    private byte[] createDataPacket(int blockNumber, byte[] data, int length) {
        byte[] packet = new byte[4 + length];
        packet[0] = 0;
        packet[1] = 3; // DATA
        packet[2] = (byte) (blockNumber >> 8);
        packet[3] = (byte) blockNumber;
        System.arraycopy(data, 0, packet, 4, length);
        return packet;
    }

    private void sendAck(int blockNumber, DatagramSocket socket, InetAddress address, int port) throws IOException {
        byte[] ack = new byte[4];
        ack[0] = 0;
        ack[1] = 4; // ACK
        ack[2] = (byte) (blockNumber >> 8);
        ack[3] = (byte) blockNumber;
        DatagramPacket ackPacket = new DatagramPacket(ack, ack.length, address, port);
        socket.send(ackPacket);
    }

    private void sendError(int errorCode, String message, InetAddress address, int port) throws IOException {
        byte[] msgBytes = message.getBytes();
        byte[] packet = new byte[4 + msgBytes.length + 1];
        packet[0] = 0;
        packet[1] = 5; // ERROR
        packet[2] = 0;
        packet[3] = (byte) errorCode;
        System.arraycopy(msgBytes, 0, packet, 4, msgBytes.length);
        packet[4 + msgBytes.length] = 0;

        DatagramSocket socket = new DatagramSocket();
        DatagramPacket errorPacket = new DatagramPacket(packet, packet.length, address, port);
        socket.send(errorPacket);
        socket.close();
    }
}
