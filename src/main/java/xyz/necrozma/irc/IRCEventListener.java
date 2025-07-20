package xyz.necrozma.irc;

public interface IRCEventListener {
    void onMessage(String channel, String sender, String message);
    void onPrivateMessage(String sender, String message);
    void onConnected();
    void onDisconnected();
}
