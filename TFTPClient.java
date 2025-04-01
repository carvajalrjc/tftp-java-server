import java.io.*;
import java.net.*;
import java.util.Scanner;

public class TFTPClient {
    private static final int TFTP_PORT = 69;
    private static final int BLOCK_SIZE = 512;

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Dirección IP del servidor: ");
        String serverIp = scanner.nextLine();
        InetAddress serverAddress = InetAddress.getByName(serverIp);

        System.out.print("¿Quieres subir (put) o descargar (get) un archivo?: ");
        String mode = scanner.nextLine().trim().toLowerCase();

        System.out.print("Nombre del archivo: ");
        String filename = scanner.nextLine();

        if (mode.equals("get")) {
            downloadFile(serverAddress, filename);
        } else if (mode.equals("put")) {
            uploadFile(serverAddress, filename);
        } else {
            System.out.println("Opción no válida.");
        }

        scanner.close();
    }

    private static void downloadFile(InetAddress serverAddress, String filename) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        socket.setSoTimeout(3000);

        byte[] rrq = createRequestPacket((byte) 1, filename, "octet");
        DatagramPacket request = new DatagramPacket(rrq, rrq.length, serverAddress, TFTP_PORT);
        socket.send(request);

        FileOutputStream fos = new FileOutputStream("descargas/" + filename);
        int expectedBlock = 1;

        while (true) {
            byte[] buffer = new byte[516];
            DatagramPacket response = new DatagramPacket(buffer, buffer.length);
            socket.receive(response);

            int opcode = buffer[1];

        // Verifica si es ERROR
        if (opcode == 5) {
            String errorMsg = new String(buffer, 4, response.getLength() - 5);
            System.out.println("Error del servidor: " + errorMsg);
            fos.close();
            new File("descargas/" + filename).delete(); // Elimina archivo vacío si se creó
            socket.close();
            return;
        }

        int blockNum = ((buffer[2] & 0xff) << 8) | (buffer[3] & 0xff);

        if (opcode == 3 && blockNum == expectedBlock) {
                fos.write(buffer, 4, response.getLength() - 4);
                sendAck(socket, response.getAddress(), response.getPort(), blockNum);
                if (response.getLength() < 516) break;
                expectedBlock++;
            } else if (opcode == 5) {
                System.out.println("Error: " + new String(buffer, 4, response.getLength() - 5));
                break;
            }
        }

        fos.close();
        socket.close();
        System.out.println("Archivo descargado.");
    }

    private static void uploadFile(InetAddress serverAddress, String filename) throws IOException {
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("Archivo no encontrado.");
            return;
        }

        DatagramSocket socket = new DatagramSocket();
        socket.setSoTimeout(3000);

        byte[] wrq = createRequestPacket((byte) 2, file.getName(), "octet");
        DatagramPacket request = new DatagramPacket(wrq, wrq.length, serverAddress, TFTP_PORT);
        socket.send(request);

        byte[] ackBuffer = new byte[4];
        DatagramPacket ackPacket = new DatagramPacket(ackBuffer, ackBuffer.length);
        socket.receive(ackPacket);

        // Verifica si es un ERROR (opcode 5)
        if (ackPacket.getData()[1] == 5) {
            int msgLength = ackPacket.getLength() - 5;
            String errorMsg = (msgLength > 0)
                ? new String(ackPacket.getData(), 4, msgLength)
                : "Error desconocido (paquete ERROR sin mensaje)";
            System.out.println("Error del servidor: " + errorMsg);
            socket.close();
            return;
        }

        FileInputStream fis = new FileInputStream(file);
        int blockNumber = 1;
        byte[] block = new byte[BLOCK_SIZE];
        int bytesRead;

        while ((bytesRead = fis.read(block)) != -1) {
            byte[] dataPacket = new byte[4 + bytesRead];
            dataPacket[0] = 0;
            dataPacket[1] = 3; // DATA
            dataPacket[2] = (byte) (blockNumber >> 8);
            dataPacket[3] = (byte) blockNumber;
            System.arraycopy(block, 0, dataPacket, 4, bytesRead);

            DatagramPacket sendPacket = new DatagramPacket(dataPacket, dataPacket.length,
                    ackPacket.getAddress(), ackPacket.getPort());
            socket.send(sendPacket);

            socket.receive(ackPacket); // ACK blockNumber
            blockNumber++;
        }

        fis.close();
        socket.close();
        System.out.println("Archivo subido.");
    }

    private static byte[] createRequestPacket(byte opcode, String filename, String mode) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(0);
        baos.write(opcode);
        baos.write(filename.getBytes());
        baos.write(0);
        baos.write(mode.getBytes());
        baos.write(0);
        return baos.toByteArray();
    }

    private static void sendAck(DatagramSocket socket, InetAddress address, int port, int blockNumber) throws IOException {
        byte[] ack = new byte[4];
        ack[0] = 0;
        ack[1] = 4; // ACK
        ack[2] = (byte) (blockNumber >> 8);
        ack[3] = (byte) blockNumber;
        DatagramPacket ackPacket = new DatagramPacket(ack, ack.length, address, port);
        socket.send(ackPacket);
    }
}
