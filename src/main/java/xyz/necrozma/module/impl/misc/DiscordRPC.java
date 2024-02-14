package xyz.necrozma.module.impl.misc;

import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.entities.RichPresence;
import xyz.necrozma.Client;
import xyz.necrozma.module.Category;
import xyz.necrozma.module.Module;
import xyz.necrozma.module.ModuleInfo;
import xyz.necrozma.notification.NotificationType;

@ModuleInfo(name = "DiscordRPC", description = "Discord Rich Presence", category = Category.MISC)
public class DiscordRPC extends Module {

    private final IPCClient ipcClient;

    public DiscordRPC() {
        ipcClient = Client.INSTANCE.getIpcClient();
    }

    public void onEnable() {
        try {
            ipcClient.connect();
        } catch (Exception e) {
            Client.INSTANCE.getNM().registerNotification("Failed to connect to Discord RPC", 5, NotificationType.ERROR);
        }
    }

    public void onDisable() {
        try {
            ipcClient.close();
        } catch (Exception e) {
            Client.INSTANCE.getNM().registerNotification("Failed to close Discord RPC", 5, NotificationType.ERROR);
        }
    }
}
