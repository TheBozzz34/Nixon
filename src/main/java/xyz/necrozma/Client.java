package xyz.necrozma;

import com.google.gson.Gson;
import lombok.Getter;
import me.zero.alpine.bus.EventBus;
import me.zero.alpine.bus.EventManager;
import me.zero.alpine.listener.Listener;
import me.zero.alpine.listener.Subscribe;
import me.zero.alpine.listener.Subscriber;
import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;
import net.arikia.dev.drpc.DiscordUser;
import net.arikia.dev.drpc.callbacks.ReadyCallback;
import net.azurewebsites.thehen101.coremod.forgepacketmanagement.ForgePacketManagement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import xyz.necrozma.event.impl.input.EventKey;
import xyz.necrozma.event.impl.update.EventUpdate;
import xyz.necrozma.gui.ModGui;
import xyz.necrozma.module.ModuleManager;
import xyz.necrozma.command.CommandManager;
import xyz.necrozma.module.impl.render.Xray;
import xyz.necrozma.util.FileUtil;
import xyz.necrozma.util.PacketHandler;
import xyz.necrozma.util.PresenceManager;
import xyz.necrozma.util.SocketManager;


import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public enum Client implements Subscriber {
    INSTANCE;

    private static final Logger logger = LogManager.getLogger();

    private ModGui clickGui;
    private Xray xray;
    private Gson gson;

    private String exePath = "";
    private String dllPath = "";

    private final Minecraft MC = Minecraft.getMinecraft();

    private String State = "Playing version " + MC.getVersion();
    private String Details = "In main menu";

    private ModuleManager MM;
    private CommandManager CM;
    private PacketHandler PH;
    private SocketManager SM;

    private DiscordRichPresence rich;

    public static final EventBus BUS = EventManager.builder()
            .setName("root/Shit")
            .setSuperListeners()
            .build();

    private final String
            name = "Nixon Client",
            version = "1.0.0",
            commandPrefix = "#",
            clientPrefix = "&7[&cNixon&7]&r ",
            author = "Necrozma";

    public final void init() throws IOException {
        Display.setTitle(name + " -> " + version);
        BUS.subscribe(this);

        gson = new Gson();

        MM = new ModuleManager();
        CM = new CommandManager();
        PH = new PacketHandler();
        SM = new SocketManager();


        clickGui = new ModGui();
        xray = new Xray();

        xray.addBlocks();

        try {
            exePath = FileUtil.ExportResource("/DiscordRPC.exe");
            dllPath = FileUtil.ExportResource("/discord_game_sdk.dll");

            logger.info("Exported DiscordRPC.exe to " + exePath + " and discord_game_sdk.dll to " + dllPath);
            logger.info("Rich presence will now start");

            boolean success = FileUtil.StartExe(exePath);

            if (success) {
                logger.info("Successfully started DiscordRPC.exe");
                PresenceManager.setPresence("In main menu", "Playing version " + MC.getVersion(), "cover", MC.getVersion());
            } else {
                logger.error("Failed to start DiscordRPC.exe");
            }
        } catch (Exception e) {
            logger.error("Failed to export DiscordRPC.exe, rich presence will not work", e);
        }

    }

    public final void onRender() {
        AtomicInteger yOffSet = new AtomicInteger(4);
        int yOffsetInc = 10;
        int squareSize = 2;
        ScaledResolution scaledResolution = new ScaledResolution(MC);

        int centerX = scaledResolution.getScaledWidth() / 2;
        int centerY = scaledResolution.getScaledHeight() / 2;
        if (MC.inGameHasFocus && !MC.gameSettings.showDebugInfo) {
            // drawRect(centerX - squareSize / 2, centerY - squareSize / 2, centerX + squareSize / 2, centerY + squareSize / 2, 0xFFFF0000);
            MM.getModules().values().forEach(module -> {
                int width = MC.fontRendererObj.getStringWidth(module.getName()) + 2;
                if (module.isToggled()) {
                    MC.ingameGUI.drawString(MC.fontRendererObj, module.getName(), 2, yOffSet.get(), 0xFF00FF00);
                    MC.ingameGUI.drawString(MC.fontRendererObj, "[" + Keyboard.getKeyName(module.getKey()) + "]", width + 2, yOffSet.get(), 0xFF00FFFF);
                } else {
                    MC.ingameGUI.drawString(MC.fontRendererObj, module.getName(), 2, yOffSet.get(), 0xFFFF0000);
                    MC.ingameGUI.drawString(MC.fontRendererObj, "[" + Keyboard.getKeyName(module.getKey()) + "]", width + 2, yOffSet.get(), 0xFF00FFFF);
                }
                yOffSet.addAndGet(yOffsetInc);
            });

            drawChromaString("Nixon Client", scaledResolution.getScaledWidth() - MC.fontRendererObj.getStringWidth("Nixon Client") - 2, 2, false);

        }
    }

    private void drawChromaString(String text, int x, int y, boolean shadow) {
        int updateCounter = MC.ingameGUI.getUpdateCounter() / 5;
        double frequency = 0.2; // speed

        int red = (int) (Math.sin(frequency * updateCounter + 0) * 127 + 128);
        int green = (int) (Math.sin(frequency * updateCounter + 2) * 127 + 128);
        int blue = (int) (Math.sin(frequency * updateCounter + 4) * 127 + 128);

        int color = (red << 16) + (green << 8) + blue;

        if (shadow) {
            MC.fontRendererObj.drawStringWithShadow(text, x, y, color);
        } else {
            MC.fontRendererObj.drawString(text, x, y, color);
        }
    }

    public final void shutdown() {
        BUS.unsubscribe(this);

        logger.info("Shutting down Discord RPC");

        boolean success = FileUtil.KillProcess("DiscordRPC.exe");
        if (success) {
            logger.info("Successfully killed DiscordRPC.exe");
        } else {
            logger.error("Failed to kill DiscordRPC.exe");
        }

        SM.close();
    }

    @Subscribe
    private final Listener<EventKey> listener = new Listener<>(e -> {
        if(this.MM != null) {
            MM.getModules().values().forEach(m -> {
                if (m.getKey() == e.getKey()) {
                    m.toggle();
                }
            });
        }
        if (e.getKey() == Keyboard.KEY_GRAVE) {
            MC.displayGuiScreen(clickGui);
        }
    });

    @Subscribe
    private final Listener<EventUpdate> listener2 = new Listener<>(e -> {
        if (MC.thePlayer != null) {
            if(MC.inGameHasFocus) {
                if (MC.isSingleplayer()) {
                    State = "In Singleplayer";
                    Details = "Playing on " + MC.getIntegratedServer().getWorldName();

                    try {
                        PresenceManager.setPresence(State, Details, "cover", MC.getVersion());
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                } else {
                    State = "In Multiplayer";
                    Details = "Playing on " + MC.getCurrentServerData().serverName;

                    try {
                        PresenceManager.setPresence(State, Details, "cover", MC.getVersion());
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }

            } else {
                State = "Game Paused";
                Details = "Playing version " + MC.getVersion();

                try {
                    PresenceManager.setPresence(State, Details, "cover", MC.getVersion());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    });
}
