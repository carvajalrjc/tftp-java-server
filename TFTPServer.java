import java.net.*;

public class TFTPServer {
    private static final int PORT = 69;

    public static void main(String[] args) {
        try (DatagramSocket serverSocket = new DatagramSocket(PORT)) {
            System.out.println("Servidor TFTP escuchando en el puerto " + PORT);

            while (true) {
                byte[] buffer = new byte[516];
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                serverSocket.receive(request);

                // Manejar cliente en un hilo (o puedes hacerlo sin hilos si prefieres)
                new Thread(new TFTPHandler(request)).start();
            }

        } catch (Exception e) {
            System.err.println("Error en el servidor TFTP: " + e.getMessage());
            e.printStackTrace();
        }
    }
}


