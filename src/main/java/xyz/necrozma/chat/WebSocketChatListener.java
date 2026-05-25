package xyz.necrozma.chat;

public interface WebSocketChatListener {
    void onMessage(String channel, String sender, String message);
    void onPrivateMessage(String sender, String message);
    void onConnected();
    void onDisconnected();
}
