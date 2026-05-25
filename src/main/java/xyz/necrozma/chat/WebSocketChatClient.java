package xyz.necrozma.chat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;
import xyz.necrozma.Client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class WebSocketChatClient {
    private static final Logger LOGGER = LogManager.getLogger(WebSocketChatClient.class);

    private final URI serverUri;
    private final String nick;
    private final String channel;
    private final WebSocketChatListener listener;
    private final ScheduledExecutorService reconnectExecutor = Executors.newSingleThreadScheduledExecutor();
    private final AtomicBoolean connected = new AtomicBoolean(false);

    private volatile WebSocketClient socket;
    private volatile boolean running = false;
    private long lastReconnectAttempt = 0;
    private static final long RECONNECT_COOLDOWN_MS = 10000; // 10 seconds

    public static final String CHAT_PREFIX = Client.INSTANCE.getChatPrefix();

    public WebSocketChatClient(String server, int port, String nick, String user, String channel, WebSocketChatListener listener) throws IOException {
        this.serverUri = buildUri(server, port);
        this.nick = nick;
        this.channel = channel;
        this.listener = listener;

        LOGGER.info("Initializing chat client. " + getDebugState());
        connect();
    }

    private URI buildUri(String server, int port) throws IOException {
        try {
            if (server.startsWith("ws://") || server.startsWith("wss://")) {
                final URI uri = new URI(server);
                if (uri.getPort() != -1 || port <= 0) {
                    return uri;
                }

                return new URI(
                        uri.getScheme(),
                        uri.getUserInfo(),
                        uri.getHost(),
                        port,
                        uri.getPath(),
                        uri.getQuery(),
                        uri.getFragment()
                );
            }

            final String scheme = port == 443 ? "wss" : "ws";
            return new URI(scheme + "://" + server + ":" + port);
        } catch (URISyntaxException e) {
            throw new IOException("Invalid websocket URI: " + server, e);
        }
    }

    private void connect() {
        running = true;
        LOGGER.info("Starting chat connection attempt. " + getDebugState());
        openSocket();
    }

    private synchronized void openSocket() {
        LOGGER.info("Opening websocket connection to " + serverUri + " (channel=" + channel + ", nick=" + nick + ")");
        socket = new WebSocketClient(serverUri) {
            @Override
            public void onOpen(ServerHandshake handshakeData) {
                connected.set(true);
                LOGGER.info("Chat websocket connected. status="
                        + (handshakeData != null ? handshakeData.getHttpStatus() : "n/a")
                        + " message="
                        + (handshakeData != null ? handshakeData.getHttpStatusMessage() : "n/a")
                        + " " + getDebugState());
                sendJoin();
                if (listener != null) {
                    listener.onConnected();
                }
            }

            @Override
            public void onMessage(String message) {
                LOGGER.debug("Chat websocket message received (" + (message == null ? 0 : message.length()) + " chars).");
                handleServerMessage(message);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                LOGGER.warn("Chat websocket closed. code=" + code + " remote=" + remote + " reason='" + reason + "' " + getDebugState());
                handleDisconnect();
            }

            @Override
            public void onError(Exception ex) {
                if (!running) {
                    LOGGER.debug("Chat websocket error ignored because client is stopped. " + ex);
                    return;
                }

                LOGGER.error("Chat websocket error. " + getDebugState() + " ex=" + ex);
                ex.printStackTrace();
                // Treat transport errors as disconnects.
                if (!isOpen()) {
                    handleDisconnect();
                }
            }
        };

        socket.connect();
    }

        private void sendJoin() {
        final WebSocketClient currentSocket = socket;
        if (currentSocket == null || !currentSocket.isOpen()) {
            LOGGER.warn("Skipping join payload because websocket is not open. " + getDebugState());
            return;
        }

        final JSONObject joinPayload = new JSONObject();
        joinPayload.put("type", "join");
        joinPayload.put("username", nick);
        joinPayload.put("channel", channel);
        LOGGER.debug("Sending join payload: " + joinPayload);
        currentSocket.send(joinPayload.toString());
    }

    public void sendMessage(String message) throws IOException {
        final WebSocketClient currentSocket = socket;
        if (!running || currentSocket == null || !currentSocket.isOpen()) {
            final String diagnosticState = getDebugState();
            LOGGER.warn("Unable to send chat message because websocket is not connected. " + diagnosticState);
            throw new IOException("WebSocket chat is not connected. " + diagnosticState);
        }

        final JSONObject payload = new JSONObject();
        payload.put("type", "chat");
        payload.put("username", nick);
        payload.put("channel", channel);
        payload.put("message", message);
        LOGGER.debug("Sending chat payload (" + (message == null ? 0 : message.length()) + " chars): " + payload);
        currentSocket.send(payload.toString());
    }

    private void handleServerMessage(String message) {
        if (message == null || message.isEmpty() || listener == null) {
            return;
        }

        try {
            final JSONObject json = new JSONObject(message);
            final String type = json.optString("type", "chat");
            final String sender = firstNonBlank(
                    json.optString("sender", null),
                    json.optString("from", null),
                    json.optString("username", null),
                    "Server"
            );
            final String content = firstNonBlank(
                    json.optString("message", null),
                    json.optString("content", null),
                    json.optString("text", null),
                    message
            );
            final String targetChannel = firstNonBlank(json.optString("channel", null), channel);

            if ("private".equalsIgnoreCase(type) || json.optBoolean("private", false)) {
                listener.onPrivateMessage(sender, content);
                return;
            }

            if ("chat".equalsIgnoreCase(type) || "message".equalsIgnoreCase(type) || "system".equalsIgnoreCase(type)) {
                listener.onMessage(targetChannel, sender, content);
                return;
            }
        } catch (Exception parseException) {
            LOGGER.debug("Failed to parse inbound message as JSON; trying fallback parser. " + parseException);
            // Fall through to plain text handling.
        }

        // Fallback for simple "sender: message" payloads.
        final int separatorIndex = message.indexOf(':');
        if (separatorIndex > 0 && separatorIndex < message.length() - 1) {
            final String sender = message.substring(0, separatorIndex).trim();
            final String content = message.substring(separatorIndex + 1).trim();
            listener.onMessage(channel, sender, content);
            return;
        }

        listener.onMessage(channel, "Server", message);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value;
            }
        }
        return "";
    }

        private void handleDisconnect() {
        final boolean wasConnected = connected.compareAndSet(true, false);
        LOGGER.info("Handling chat disconnect. wasConnected=" + wasConnected + " " + getDebugState());

        if (wasConnected && listener != null) {
            try {
                listener.onDisconnected();
            } catch (Exception listenerException) {
                LOGGER.warn("Chat disconnect listener threw an exception. " + listenerException);
                listenerException.printStackTrace();
            }
        }

        scheduleReconnect();
    }

    private void scheduleReconnect() {
        if (!running) {
            LOGGER.debug("Reconnect skipped because chat client is not running.");
            return;
        }

        final long now = System.currentTimeMillis();
        if (now - lastReconnectAttempt <= RECONNECT_COOLDOWN_MS) {
            LOGGER.debug("Reconnect skipped due to cooldown. waitRemainingMs="
                    + (RECONNECT_COOLDOWN_MS - (now - lastReconnectAttempt))
                    + " " + getDebugState());
            return;
        }
        lastReconnectAttempt = now;
        LOGGER.info("Scheduling chat reconnect in " + RECONNECT_COOLDOWN_MS + " ms. " + getDebugState());

        reconnectExecutor.schedule(() -> {
            if (!running) {
                LOGGER.debug("Reconnect task aborted because chat client is not running.");
                return;
            }

            LOGGER.info("Executing scheduled chat reconnect. " + getDebugState());
            openSocket();
        }, RECONNECT_COOLDOWN_MS, TimeUnit.MILLISECONDS);
    }

    public void close() {
        running = false;
        lastReconnectAttempt = 0;
        LOGGER.info("Closing chat client. " + getDebugState());

        final WebSocketClient currentSocket = socket;
        socket = null;
        if (currentSocket != null) {
            try {
                currentSocket.closeBlocking();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.warn("Interrupted while closing chat websocket. " + e);
                e.printStackTrace();
            } catch (Exception e) {
                LOGGER.warn("Error while closing chat websocket. " + e);
                e.printStackTrace();
            }
        }

        reconnectExecutor.shutdownNow();
    }

    public void tick() {
        final WebSocketClient currentSocket = socket;
        if (!running || currentSocket == null) {
            return;
        }

        if (!currentSocket.isOpen()) {
            LOGGER.debug("Tick detected non-open websocket; requesting reconnect. " + getDebugState());
            scheduleReconnect();
        }
    }

    public String getDebugState() {
        final WebSocketClient currentSocket = socket;
        if (currentSocket == null) {
            return String.format(
                    "uri=%s, channel=%s, nick=%s, running=%s, connected=%s, socket=null",
                    serverUri,
                    channel,
                    nick,
                    running,
                    connected.get()
            );
        }

        return String.format(
                "uri=%s, channel=%s, nick=%s, running=%s, connected=%s, open=%s, closed=%s",
                serverUri,
                channel,
                nick,
                running,
                connected.get(),
                currentSocket.isOpen(),
                currentSocket.isClosed()
        );
    }
}
