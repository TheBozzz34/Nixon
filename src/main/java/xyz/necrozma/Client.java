package xyz.necrozma;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
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
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import xyz.necrozma.event.impl.input.EventKey;
import xyz.necrozma.event.impl.update.EventUpdate;
import xyz.necrozma.gui.ModGui;
import xyz.necrozma.module.ModuleManager;
import xyz.necrozma.command.CommandManager;
import xyz.necrozma.module.impl.render.Xray;
import xyz.necrozma.util.ChatUtil;


import java.util.concurrent.atomic.AtomicInteger;

import static net.minecraft.client.gui.Gui.drawRect;

@Getter
public enum Client implements Subscriber {
    INSTANCE;

    private ModGui clickGui;
    private Xray xray;

    private final Minecraft MC = Minecraft.getMinecraft();

    private String State = "Playing version " + MC.getVersion();
    private String Details = "In main menu";

    private ModuleManager MM;
    private CommandManager CM;

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

    public final void init() {
        Display.setTitle(name + " -> " + version);
        BUS.subscribe(this);

        MM = new ModuleManager();
        CM = new CommandManager();

        clickGui = new ModGui();
        xray = new Xray();

        xray.addBlocks();

        DiscordEventHandlers handlers = new DiscordEventHandlers.Builder().setReadyEventHandler((user) -> {
            System.out.println("Welcome " + user.username + "#" + user.discriminator + "!");
        }).build();

        DiscordRPC.discordInitialize("824317166357577728", handlers, true);

        createNewPresence();
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

                    updatePresence(rich, State, Details);
                } else {
                    State = "In Multiplayer";
                    Details = "Playing on " + MC.getCurrentServerData().serverName;

                    updatePresence(rich, State, Details);
                }

            } else {
                State = "Game Paused";
                Details = "Playing version " + MC.getVersion();

                updatePresence(rich, State, Details);
            }
        }
    });

    public static class ReadyEvent implements ReadyCallback {
        @Override
        public void apply(DiscordUser discordUser) {
            System.out.println("Discord's ready!");
        }
    }

    public void createNewPresence() {
        rich = new DiscordRichPresence.Builder(State).setDetails(Details).build();
        rich.largeImageKey = "cover";
        rich.largeImageText = MC.getVersion();
        DiscordRPC.discordUpdatePresence(rich);
    }

    public void updatePresence(DiscordRichPresence rich, String State, String Details) {
        rich.state = State;
        rich.details = Details;
        DiscordRPC.discordUpdatePresence(rich);
    }

}
