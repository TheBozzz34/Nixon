package xyz.necrozma.irc;

import xyz.necrozma.Client;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IRCClient {
    private String server;
    private int port;
    private String nick;
    private String user;
    private String channel;

    private Socket socket;
    private BufferedWriter writer;
    private BufferedReader reader;

    private IRCEventListener listener;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean running = false;

    private long lastReconnectAttempt = 0;
    private static final long RECONNECT_COOLDOWN_MS = 10000; // 10 seconds

    public static final String IRC_PREFIX = Client.INSTANCE.getIRCPrefix();

    public IRCClient(String server, int port, String nick, String user, String channel, IRCEventListener listener) throws IOException {
        this.server = server;
        this.port = port;
        this.nick = nick;
        this.user = user;
        this.channel = channel;
        this.listener = listener;

        connect();
    }

    private void connect() throws IOException {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }

        socket = new Socket(server, port);
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        running = true;

        login();
        startListening(); // now handles joinChannel when ready
    }



    private void login() throws IOException {
        sendRaw("NICK " + nick);
        sendRaw("USER " + user + " 0 * :" + user);
    }

    private void joinChannel() throws IOException {
        sendRaw("JOIN " + channel);
    }

    public void sendMessage(String message) throws IOException {
        sendRaw("PRIVMSG " + channel + " :" + message);
    }

    private void sendRaw(String data) throws IOException {
        writer.write(data + "\r\n");
        writer.flush();
    }

    private void startListening() {
        executor.submit(() -> {
            boolean connected = false;
            try {
                String line;
                while (running && (line = reader.readLine()) != null) {
                    System.out.println("<< " + line);

                    if (line.startsWith("PING ")) {
                        sendRaw("PONG " + line.substring(5));
                        continue;
                    }

                    if (!connected && line.contains(" 001 ")) {
                        connected = true;
                        joinChannel(); // <- move JOIN here
                        if (listener != null) listener.onConnected();
                    }

                    handleServerMessage(line);
                }
            } catch (IOException e) {
                if (running) System.err.println("Connection error: " + e.getMessage());
            } finally {
                if (listener != null) listener.onDisconnected();
            }
        });
    }


    private void handleServerMessage(String line) {
        // Parse messages with prefix :sender COMMAND args
        if (line.startsWith(":")) {
            String[] parts = line.split(" ", 4);
            String sender = parts[0].substring(1);
            String command = parts[1];

            if ("PRIVMSG".equals(command) && parts.length >= 4) {
                String target = parts[2];
                String message = parts[3].startsWith(":") ? parts[3].substring(1) : parts[3];

                String nick = sender.split("!")[0];

                if (target.equals(nick)) {
                    // Private message
                    if (listener != null) listener.onPrivateMessage(nick, message);
                } else {
                    // Channel message
                    if (listener != null) listener.onMessage(target, nick, message);
                }
            }
        }
    }

    public void close() {
        try {
            running = false;
            sendRaw("QUIT :Bye");
            socket.close();
        } catch (IOException ignored) {
        } finally {
            executor.shutdownNow();
        }
    }

    public void tick() {
        if (!running || socket == null || socket.isClosed() || !socket.isConnected()) {
            long now = System.currentTimeMillis();
            if (now - lastReconnectAttempt > RECONNECT_COOLDOWN_MS) {
                lastReconnectAttempt = now;
                System.out.println("Attempting IRC reconnect...");
                attemptReconnect();
            }
        }
    }

    private void attemptReconnect() {
        executor.submit(() -> {
            try {
                close(); // Clean up existing resources if any
                connect(); // Reconnect
                if (listener != null) listener.onConnected();
                sendMessage("Reconnected to IRC.");
            } catch (IOException e) {
                System.err.println("IRC reconnect failed: " + e.getMessage());
            }
        });
    }
}

