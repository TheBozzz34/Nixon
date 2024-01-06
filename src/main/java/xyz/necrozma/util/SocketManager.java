package xyz.necrozma.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.*;

public class SocketManager {
    private final DatagramSocket socket;
    private final InetAddress address;

    private final int port;

    private final Logger logger = LogManager.getLogger();

    public SocketManager() throws SocketException, UnknownHostException {
        socket = new DatagramSocket();
        address = InetAddress.getByName("127.0.0.1");
        this.port = 8888;
    }

    public void send(String message) throws IOException {
        byte[] buffer = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
        socket.send(packet);

        // logger.info("Sent packet: " + message);

    }

    public void close() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}
