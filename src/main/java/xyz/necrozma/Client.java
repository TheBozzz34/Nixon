package xyz.necrozma;


import lombok.Setter;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import xyz.necrozma.discord.IPCClient;
import xyz.necrozma.discord.IPCListener;
import xyz.necrozma.discord.entities.RichPresence;
import xyz.necrozma.exception.CommandException;
import xyz.necrozma.gui.render.RenderUtil;
import xyz.necrozma.pathing.Path;
import lombok.Getter;
import me.zero.alpine.bus.EventBus;
import me.zero.alpine.bus.EventManager;
import me.zero.alpine.listener.Listener;
import me.zero.alpine.listener.Subscribe;
import me.zero.alpine.listener.Subscriber;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.Display;
import xyz.necrozma.event.impl.input.EventKey;
import xyz.necrozma.event.impl.motion.PreMotionEvent;
import xyz.necrozma.event.impl.render.Render3DEvent;
import xyz.necrozma.gui.clickgui.ClickGUI;
import xyz.necrozma.module.Module;
import xyz.necrozma.module.ModuleManager;
import xyz.necrozma.command.CommandManager;
import xyz.necrozma.module.impl.render.Xray;
import xyz.necrozma.notification.NotificationManager;
import xyz.necrozma.notification.NotificationType;
import xyz.necrozma.util.*;


import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
public enum Client implements Subscriber {
    INSTANCE;

    private static final Logger logger = LogManager.getLogger();

    public ClickGUI clickGUI;
    private Xray xray;

    private final Minecraft MC = Minecraft.getMinecraft();

    private ModuleManager MM;
    private CommandManager CM;
    private PacketHandler PH;
    private NotificationManager NM;
    private StatsUtil SU;

    @Setter
    private Path path;

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

    private IPCClient ipcClient = new IPCClient(824317166357577728L);

    public final void init() throws IOException {
        ipcClient.setListener(new IPCListener(){
            @Override
            public void onReady(IPCClient client)
            {
                RichPresence.Builder builder = new RichPresence.Builder();
                builder.setState("Client ver. 1.0.0")
                        .setDetails("MC 1.8.9")
                        .setStartTimestamp(OffsetDateTime.now())
                        .setLargeImage("cover", "1.8.9");
                client.sendRichPresence(builder.build());
            }
        });

        if (!FileUtil.riseDirectoryExists()) {
            FileUtil.createRiseDirectory();
        }

        if (!FileUtil.exists("Config" + File.separator)) {
            FileUtil.createDirectory("Config" + File.separator);
        }

        Display.setTitle(name + " -> " + version);
        BUS.subscribe(this);

        MM = new ModuleManager();
        CM = new CommandManager();
        PH = new PacketHandler();
        NM = new NotificationManager();
        SU = new StatsUtil();


        clickGUI = new ClickGUI();
        xray = new Xray();
        xray.addBlocks();

        loadConfig();

        try {
            ipcClient.connect();
        } catch (Exception e) {
            logger.error("Failed to connect to Discord RPC");
        }

        // logger.info(SU.getStats());
    }

    private List<Vec3> vecPath = new ArrayList<>();
    public final void onRender() {
        if (!MC.inGameHasFocus) return;

        // Clear vecPath if path is finished or null
        if (path == null || path.isFinished()) {
            vecPath.clear();
            return;
        }

        // Fetch next point from path
        BlockPos nextPoint = path.getNextPoint();
        if (nextPoint != null) {
            // Synchronize access to vecPath to prevent ConcurrentModificationException
            synchronized (vecPath) {
                vecPath.add(new Vec3(nextPoint.getX(), nextPoint.getY(), nextPoint.getZ()));
            }
        }
    }


    public final void shutdown() {
        BUS.unsubscribe(this);

        saveConfig();

        try {
            ipcClient.close();
        } catch (Exception e) {
            logger.error("Failed to close Discord RPC");
        }

    }

    private void loadConfig() {
        final String config = FileUtil.loadFile("settings.txt");
        if (config == null) return;
        final String[] configLines = config.split("\r\n");


        boolean gotConfigVersion = false;
        for (final String line : configLines) {
            if (line == null) return;


            final String[] split = line.split("_");

            if (split[0].contains("Nixon")) {
                if (split[1].contains("Version")) {
                    gotConfigVersion = true;

                    final String clientVersion = version;
                    final String configVersion = split[2];

                    if (!clientVersion.equalsIgnoreCase(configVersion)) {
                        ChatUtil.sendMessage("This config was made in a different version of Rise! Incompatibilities are expected.");
                        this.getNM().registerNotification(
                                "This config was made in a different version of Rise! Incompatibilities are expected.", NotificationType.WARNING
                        );
                    }
                }
            }


            if (split[0].contains("Toggle")) {
                if (split[2].contains("true")) {
                    Module module = Client.INSTANCE.getMM().getModuleFromString(split[1]);
                    if (module == null) {
                        throw new CommandException("Module " + split[1] + " not found!");
                    } else {
                        module.toggle();
                    }
                }
            }

            /*
            final Settings setting = getMM().getSettings(split[1], split[2]);

            if (split[0].contains("BooleanSetting") && setting instanceof BooleanSetting) {
                if (split[3].contains("true")) {
                    ((BooleanSetting) setting).enabled = true;
                }

                if (split[3].contains("false")) {
                    ((BooleanSetting) setting).enabled = false;
                }
            }

            if (split[0].contains("NumberSetting") && setting instanceof NumberSetting)
                ((NumberSetting) setting).setValue(Double.parseDouble(split[3]));

            if (split[0].contains("ModeSetting") && setting instanceof ModeSetting)
                ((ModeSetting) setting).set(split[3]);

            if (split[0].contains("Bind")) {
                final Module m = getModuleManager().getModule(split[1]);

                if (m != null)
                    Objects.requireNonNull(m).setKeyBind(Integer.parseInt(split[2]));
            }

             */
        }
        if (!gotConfigVersion) {
            ChatUtil.sendMessage("This config was made in a different version of Rise! Incompatibilities are expected.");
            getNM().registerNotification(
                    "This config was made in a different version of Rise! Incompatibilities are expected.", NotificationType.WARNING
            );
        }
    }

    private void saveConfig() {
        final StringBuilder configBuilder = new StringBuilder();
        configBuilder.append("Nixon_Version_").append(version).append("\r\n");

        for (final Module m : getMM().getModules().values()) {
            final String moduleName = m.getName();
            configBuilder.append("Toggle_").append(moduleName).append("_").append(m.isToggled()).append("\r\n");
        }

        FileUtil.saveFile("settings.txt", true, configBuilder.toString());
    }

    @Subscribe
    private final Listener<Render3DEvent> lister0 = new Listener<>(e -> {
        if (this.MM != null) {
            MM.getModules().values().stream().filter(Module::isToggled).forEach(m -> m.onRender3DEvent(e));
        }
    });

    @Subscribe
    private final Listener<PreMotionEvent> listener1 = new Listener<>(e -> {

        RenderUtil.renderBreadCrumbs(vecPath);

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
                    // NM.registerNotification(m.getName() + " toggled!", 3000, NotificationType.NOTIFICATION);
                }
            });
        }
    });
}
