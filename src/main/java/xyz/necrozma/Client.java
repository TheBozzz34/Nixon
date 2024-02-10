package xyz.necrozma;

import com.google.gson.Gson;
import lombok.Getter;
import me.zero.alpine.bus.EventBus;
import me.zero.alpine.bus.EventManager;
import me.zero.alpine.listener.Listener;
import me.zero.alpine.listener.Subscribe;
import me.zero.alpine.listener.Subscriber;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import xyz.necrozma.event.impl.input.EventKey;
import xyz.necrozma.event.impl.motion.PreMotionEvent;
import xyz.necrozma.event.impl.render.Render3DEvent;
import xyz.necrozma.gui.ModGui;
import xyz.necrozma.gui.clickgui.ClickGUI;
import xyz.necrozma.module.Module;
import xyz.necrozma.module.ModuleManager;
import xyz.necrozma.command.CommandManager;
import xyz.necrozma.module.impl.render.Xray;
import xyz.necrozma.notification.NotificationManager;
import xyz.necrozma.notification.NotificationType;
import xyz.necrozma.util.*;


import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public enum Client implements Subscriber {
    INSTANCE;

    private static final Logger logger = LogManager.getLogger();

    public ClickGUI clickGUI;
    private Xray xray;
    private Gson gson;


    private final Minecraft MC = Minecraft.getMinecraft();


    private ModuleManager MM;
    private CommandManager CM;
    private PacketHandler PH;
    private NotificationManager NM;

    private boolean authed;


    public static final EventBus BUS = EventManager.builder()
            .setName("root/Shit")
            .setSuperListeners()
            .build();

    private final String
            name = "Nixon",
            version = "1.0.0",
            commandPrefix = "#",
            clientPrefix = "&7[&cNixon&7]&r ",
            author = "Necrozma";

    public final void init() throws IOException {

        // authed = HWID.checkHWID(); el broken

        Display.setTitle(name + " -> " + version);
        BUS.subscribe(this);

        gson = new Gson();

        MM = new ModuleManager();
        CM = new CommandManager();
        PH = new PacketHandler();
        NM = new NotificationManager();


        clickGUI = new ClickGUI();
        xray = new Xray();

        xray.addBlocks();


    }

    public final void onRender() {
        AtomicInteger yOffSet = new AtomicInteger(4);
        int yOffsetInc = 10;
        int squareSize = 2;
        ScaledResolution scaledResolution = new ScaledResolution(MC);

        int centerX = scaledResolution.getScaledWidth() / 2;
        int centerY = scaledResolution.getScaledHeight() / 2;

        /*
        if(!MC.inGameHasFocus) {
            if(authed) {
                MC.ingameGUI.drawString(MC.fontRendererObj, "Logged in!", 2, 2, 0xFF00FF00);
            } else {
                MC.ingameGUI.drawString(MC.fontRendererObj, "Not logged in!", 2, 2, 0xFFFF0000);
            }
        }

         */

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
    private final Listener<Render3DEvent> lister0 = new Listener<>(e -> {
        if (this.MM != null) {
            MM.getModules().values().stream().filter(Module::isToggled).forEach(m -> m.onRender3DEvent(e));
        }
    });

    @Subscribe
    private final Listener<PreMotionEvent> listener1 = new Listener<>(e -> {
        if (this.MM != null) {
            MM.getModules().values().stream().filter(Module::isToggled).forEach(m -> m.onPreMotion(e));
        }
    });

    @Subscribe
    private final Listener<EventKey> listener2 = new Listener<>(e -> {
        if(this.MM != null) {
            MM.getModules().values().forEach(m -> {
                if (m.getKey() == e.getKey()) {
                    m.toggle();
                    NM.registerNotification(m.getName() + " toggled!", 3000, NotificationType.NOTIFICATION);
                }
            });
        }
        /*
        if (e.getKey() == Keyboard.KEY_GRAVE) {
            MC.displayGuiScreen(clickGui);
        }

         */
    });
}
