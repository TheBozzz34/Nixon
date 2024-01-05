package xyz.necrozma;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import me.zero.alpine.bus.EventBus;
import me.zero.alpine.bus.EventManager;
import me.zero.alpine.listener.Listener;
import me.zero.alpine.listener.Subscribe;
import me.zero.alpine.listener.Subscriber;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import xyz.necrozma.event.impl.input.EventKey;
import xyz.necrozma.gui.ModGui;
import xyz.necrozma.module.ModuleManager;
import xyz.necrozma.command.CommandManager;
import xyz.necrozma.util.ChatUtil;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static net.minecraft.client.gui.Gui.drawRect;

@Getter
public enum Client implements Subscriber {
    INSTANCE;

    private ModGui clickGui;

    private final Minecraft MC = Minecraft.getMinecraft();

    private ModuleManager MM;
    private CommandManager CM;

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

        // ChatUtil.sendMessage("Key typed: " + e.getKey());

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

}
