package xyz.necrozma.util;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import xyz.necrozma.Client;

public final class ChatUtil {

    public static String fix(String string) {
        return string.replace("&", "§")
                .replace(">>", "»")
                .replace("<<", "«")
                .replace("->", "→")
                .replace("<-", "←");
    }

    public static void sendMessage(final String msg) {
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(fix(msg)));
    }

    public static void sendMessage(final String msg, final boolean prefix) {
        if(prefix) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(fix( Client.INSTANCE.getClientPrefix() + msg)));
        } else {
            sendMessage(msg);
        }
    }


}
