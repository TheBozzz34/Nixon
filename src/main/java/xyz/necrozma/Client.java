package xyz.necrozma;


import de.florianmichael.viamcp.ViaMCP;
import lombok.Setter;
import net.minecraft.util.Session;
import tv.twitch.chat.Chat;
import xyz.necrozma.auth.AuthenticationService;
import xyz.necrozma.discord.IPCClient;
import xyz.necrozma.discord.IPCListener;
import xyz.necrozma.discord.entities.RichPresence;
import xyz.necrozma.event.Event;
import xyz.necrozma.event.EventHandler;
import xyz.necrozma.event.impl.input.MoveButtonEvent;
import xyz.necrozma.event.impl.motion.BlockCollideEvent;
import xyz.necrozma.event.impl.motion.MoveEvent;
import xyz.necrozma.event.impl.motion.StrafeEvent;
import xyz.necrozma.event.impl.packet.PacketReceiveEvent;
import xyz.necrozma.event.impl.render.Render2DEvent;
import xyz.necrozma.event.impl.update.EventUpdate;
import xyz.necrozma.gui.guitheme.GuiTheme;
import xyz.necrozma.gui.guitheme.Theme;
import xyz.necrozma.gui.guitheme.ThemeUtil;
import xyz.necrozma.gui.strikeless.StrikeGUI;
import xyz.necrozma.irc.IRCClient;
import xyz.necrozma.irc.IRCEventListener;
import xyz.necrozma.login.AuthenticationResult;
import xyz.necrozma.notification.NotificationType;
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
//import xyz.necrozma.gui.clickgui.ClickGUI;
import xyz.necrozma.gui.ClickGuiNG.ClickGUI;
import xyz.necrozma.module.Module;
import xyz.necrozma.module.ModuleManager;
import xyz.necrozma.command.CommandManager;
import xyz.necrozma.module.impl.render.Xray;
import xyz.necrozma.notification.NotificationManager;
import xyz.necrozma.settings.Settings;
import xyz.necrozma.settings.impl.BooleanSetting;
import xyz.necrozma.settings.impl.ModeSetting;
import xyz.necrozma.settings.impl.NumberSetting;
import xyz.necrozma.util.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Objects;


@Getter
public enum Client implements Subscriber {
    INSTANCE;

    private static final Logger logger = LogManager.getLogger();

    StrikeGUI strikeGUI;
    private Xray xray;

    private final Minecraft MC = Minecraft.getMinecraft();

    private ModuleManager MM;
    private CommandManager CM;
    private PacketHandler PH;
    private NotificationManager NM;
    private StatsUtil SU;
    private TokenManager tokenManager;
    private AuthenticationService authService;
    private IRCClient ircClient;
    public GuiTheme guiTheme;
    public static long timeJoinedServer;
    public static int totalKills;
    public static int totalDeaths;
    public static float distanceRan;
    public static float distanceFlew;
    public static float distanceJumped;
    public static int amountOfModulesOn;
    public static int amountOfVoidSaves;
    public static int amountOfConfigsLoaded;
    public static String lastLoggedAccount;

    //public static String CLIENT_NAME = "Rise", CLIENT_VERSION = "Lucas x Almir Edition";
    public static int CLIENT_THEME_COLOR_DEFAULT = new Color(159, 24, 242).hashCode();
    public static int CLIENT_THEME_COLOR = new Color(159, 24, 242).hashCode();
    public static int CLIENT_THEME_COLOR_BRIGHT = new Color(185, 69, 255).hashCode();
    public static Color CLIENT_THEME_COLOR_BRIGHT_COLOR = new Color(185, 69, 255);

    public static String ip;

    public String username = "Player";

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
            IRCPrefix = ";",
            clientPrefix = "&7[&cNixon&7]&r ",
            author = "Necrozma";

    private final IPCClient ipcClient = new IPCClient(824317166357577728L);

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

        if (!FileUtil.clientDirectoryExists()) {
            FileUtil.createClientDirectory();
        }

        Display.setTitle(name + " -> " + version);
        BUS.subscribe(this);

        MM = new ModuleManager();
        CM = new CommandManager();
        PH = new PacketHandler();
        NM = new NotificationManager();
        SU = new StatsUtil();
        tokenManager = new TokenManager("1.0.0");
        authService = new AuthenticationService(tokenManager);
        guiTheme = new GuiTheme();

        try {
            ViaMCP.create();

            // In case you want a version slider like in the Minecraft options, you can use this code here, please choose one of those:

            ViaMCP.INSTANCE.initAsyncSlider(); // For top left aligned slider
            //ViaMCP.INSTANCE.initAsyncSlider(x, y, width (min. 110), height (recommended 20)); // For custom position and size slider
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            ircClient = new IRCClient(
                    "10.0.0.177",
                    6667,
                    "JavaBot123",
                    "JavaBot123",
                    "#chat",
                    new IRCEventListener() {
                        @Override
                        public void onMessage(String channel, String sender, String message) {
                            //System.out.println("[" + channel + "] <" + sender + ">: " + message);
                            ChatUtil.sendMessage("&f[&bIRC&f] &7<" + sender + "> " + message);
                        }

                        @Override
                        public void onPrivateMessage(String sender, String message) {
                            System.out.println("[PM] <" + sender + ">: " + message);
                        }

                        @Override
                        public void onConnected() {
                            System.out.println("Connected to IRC.");
                        }

                        @Override
                        public void onDisconnected() {
                            System.out.println("Disconnected from IRC.");
                        }
                    }
            );
            ircClient.sendMessage("Hello from Java!");
        } catch (Exception e) {
            logger.error("Failed to initialize IRC client", e);
        }


        strikeGUI = new StrikeGUI();
        xray = new Xray();
        xray.addBlocks();


        authenticateUser();


        try {
            ipcClient.connect();
        } catch (Exception e) {
            logger.error("Failed to connect to Discord RPC");
        }

        if (!FileUtil.clientDirectoryExists()) {
            FileUtil.createClientDirectory();
        }

        if (!FileUtil.exists("Config" + File.separator)) {
            FileUtil.createDirectory("Config" + File.separator);
        }

        loadConfig();


    }


    public void loadConfig() {
        final String config = FileUtil.loadFile("settings.txt");
        if (config == null) return;
        final String[] configLines = config.split("\r\n");

        for (final Module m : Client.INSTANCE.getMM().getModules().values()) {
            if (m.isToggled()) {
                m.toggle();
            }
        }

        boolean gotConfigVersion = false;
        for (final String line : configLines) {
            if (line == null || line.trim().isEmpty()) continue;

            final String[] split = line.split("_");
            if (split.length < 2) {
                logger.warn("Skipping malformed config line: {}", line);
                continue;
            }

            if (split[0].contains("Client")) {
                if (split.length > 2 && split[1].contains("Version")) {
                    gotConfigVersion = true;

                    final String clientVersion = Client.INSTANCE.getVersion();
                    final String configVersion = split[2];

                    if (!clientVersion.equalsIgnoreCase(configVersion)) {
                        logger.warn("Client version does not match config version!");
                    }
                }
                continue;
            }

            if (split[0].contains("MainMenuTheme")) {
                if (split.length > 1) {
                    getGuiTheme().setCurrentTheme(Theme.valueOf(split[1]));
                }
                continue;
            }

            if (split[0].contains("ClientName")) {
                ThemeUtil.setCustomClientName(split.length > 1 ? split[1] : "");
                continue;
            }

            if (split[0].contains("Toggle")) {
                if (split.length > 2 && split[2].contains("true")) {
                    if (getMM().getModuleFromString(split[1]) != null) {
                        final Module module = Objects.requireNonNull(getMM().getModuleFromString(split[1]));

                        if (!module.isToggled()) {
                            module.toggle();
                        }
                    }
                }
                continue;
            }

            if (split.length < 3) continue;

            final Settings setting = getMM().getSetting(split[1], split[2]);
            if (setting == null) continue;

            if (split[0].contains("BooleanSetting") && setting instanceof BooleanSetting) {
                if (split.length > 3) {
                    ((BooleanSetting) setting).enabled = split[3].contains("true");
                }
            }

            if (split[0].contains("NumberSetting") && setting instanceof NumberSetting) {
                if (split.length > 3) {
                    try {
                        ((NumberSetting) setting).setValue(Double.parseDouble(split[3]));
                    } catch (NumberFormatException e) {
                        logger.warn("Invalid number in config: " + split[3]);
                    }
                }
            }

            if (split[0].contains("ModeSetting") && setting instanceof ModeSetting) {
                if (split.length > 3) {
                    ((ModeSetting) setting).set(split[3]);
                }
            }

            if (split[0].contains("Bind")) {
                if (split.length > 2) {
                    final Module m = getMM().getModuleFromString(split[1]);
                    if (m != null) {
                        try {
                            m.setKey(Integer.parseInt(split[2]));
                        } catch (NumberFormatException e) {
                            logger.warn("Invalid key bind in config: " + split[2]);
                        }
                    }
                }
            }
        }
        if (!gotConfigVersion) {
            logger.warn("Config file does not contain version information!");
        }
    }

    private void saveConfig() {
        final StringBuilder configBuilder = new StringBuilder();
        configBuilder.append("Client_Version_").append(Client.INSTANCE.getVersion()).append("\r\n");
        //configBuilder.append("PlayMusic_").append(Minecraft.getMinecraft().riseMusicTicker.shouldKeepPlaying).append("\r\n");
        configBuilder.append("MainMenuTheme_").append(getGuiTheme().getCurrentTheme()).append("\r\n");
        //configBuilder.append("ClientName_").append(ThemeUtil.getCustomClientName()).append("\r\n");

        for (final Module m : getMM().getModules().values()) {
            final String moduleName = m.getName();
            configBuilder.append("Toggle_").append(moduleName).append("_").append(m.isToggled()).append("\r\n");

            for (final Settings s : m.getSettings()) {
                if (s instanceof BooleanSetting) {
                    configBuilder.append("BooleanSetting_").append(moduleName).append("_").append(s.name).append("_").append(((BooleanSetting) s).enabled).append("\r\n");
                }
                if (s instanceof NumberSetting) {
                    configBuilder.append("NumberSetting_").append(moduleName).append("_").append(s.name).append("_").append(((NumberSetting) s).value).append("\r\n");
                }
                if (s instanceof ModeSetting) {
                    configBuilder.append("ModeSetting_").append(moduleName).append("_").append(s.name).append("_").append(((ModeSetting) s).getMode()).append("\r\n");
                }
            }
            configBuilder.append("Bind_").append(moduleName).append("_").append(m.getKey()).append("\r\n");
        }

        FileUtil.saveFile("settings.txt", true, configBuilder.toString());
    }


    public final void onRender() {
        if (!MC.inGameHasFocus) return;
    }

    private void authenticateUser() {
        System.out.println("Starting authentication process...");

        if (authService.authenticate()) {
            System.out.println("Authentication successful!");

            final AuthenticationResult result = authService.getLastAuthResult();
            if (result != null) {
                String usernameIn = result.getMinecraftUsername();
                String playerIDIn = result.getUuid();
                String tokenIn = result.getAccessToken();
                String sessionTypeIn = "legacy";

                if (usernameIn == null || playerIDIn == null || tokenIn == null) {
                    System.err.println("Authentication result contains null values");
                    return;
                }

                System.out.println("Creating Minecraft session...");
                username = usernameIn;
                MC.setSession(new Session(
                        usernameIn,
                        playerIDIn,
                        tokenIn,
                        sessionTypeIn));

                System.out.println("Session created successfully!");
            } else {
                System.err.println("Authentication result is null");
            }
        }
    }


    public final void shutdown() {
        BUS.unsubscribe(this);


        try {
            ipcClient.close();
        } catch (Exception e) {
            logger.error("Failed to close Discord RPC");
        }

        if (ircClient != null) {
            ircClient.close();
            System.out.println("IRC client disconnected.");
        }

        saveConfig();

    }

    @Subscribe
    private final Listener<Event> EventListener = new Listener<>(EventHandler::handle);

}