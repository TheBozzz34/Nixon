package xyz.necrozma.util;

import xyz.necrozma.Client;

import java.io.IOException;

public class PresenceManager {

    public static void setPresence(String details, String state, String largeImageKey, String largeImageText) throws IOException {
        Message message = new Message();
        message.setDetails(details);
        message.setState(state);
        message.setLargeImageKey(largeImageKey);
        message.setLargeImageText(largeImageText);
        Client.INSTANCE.getSM().send(Client.INSTANCE.getGson().toJson(message));

    }
}
