package xyz.necrozma;


import lombok.Setter;
import net.minecraft.util.Session;
import xyz.necrozma.auth.AuthenticationService;
import xyz.necrozma.discord.IPCClient;
import xyz.necrozma.discord.IPCListener;
import xyz.necrozma.discord.entities.RichPresence;
import xyz.necrozma.gui.strikeless.StrikeGUI;
import xyz.necrozma.irc.IRCClient;
import xyz.necrozma.irc.IRCEventListener;
import xyz.necrozma.login.AuthenticationResult;
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
import xyz.necrozma.util.*;

import java.io.IOException;
import java.time.OffsetDateTime;


@Getter
public enum Client implements Subscriber {
    INSTANCE;

    private static final Logger logger = LogManager.getLogger();

    public ClickGUI clickGUI;
    public StrikeGUI strikeGUI;
    private Xray xray;

    private final Minecraft MC = Minecraft.getMinecraft();

    private ModuleManager MM;
    private CommandManager CM;
    private PacketHandler PH;
    private NotificationManager NM;
    private StatsUtil SU;
    private ConfigManager configManager;
    private AuthenticationService authService;
    private IRCClient ircClient;

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
        configManager = new ConfigManager("1.0.0", MM);
        authService = new AuthenticationService(configManager);

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


        clickGUI = new ClickGUI();
        strikeGUI = new StrikeGUI();
        xray = new Xray();
        xray.addBlocks();

        if (configManager.loadConfig()) {
            System.out.println("Config loaded successfully!");
        }

        authenticateUser();


        try {
            ipcClient.connect();
        } catch (Exception e) {
            logger.error("Failed to connect to Discord RPC");
        }
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

        if (configManager.saveConfig()) {
            System.out.println("Config saved successfully!");
        }

        try {
            ipcClient.close();
        } catch (Exception e) {
            logger.error("Failed to close Discord RPC");
        }

        if (ircClient != null) {
            ircClient.close();
            System.out.println("IRC client disconnected.");
        }



    }

    @Subscribe
    private final Listener<Render3DEvent> lister0 = new Listener<>(e -> {


        if (this.MM != null) {
            MM.getModules().values().stream().filter(Module::isToggled).forEach(m -> m.onRender3DEvent(e));
        }
    });

    @Subscribe
    private final Listener<PreMotionEvent> listener1 = new Listener<>(e -> {

        if (ircClient != null) {
            ircClient.tick();
        }


        if (this.MM != null) {
            MM.getModules().values().stream().filter(Module::isToggled).forEach(m -> m.onPreMotion(e));

            if (MC.currentScreen instanceof ClickGUI ) {
                MM.getModules().values().stream().filter(Module::isToggled).forEach(Module::onUpdateAlwaysInGui);
            }

        }
    });

    @Subscribe
    private final Listener<EventKey> listener2 = new Listener<>(e -> {
        if(this.MM != null) {
            MM.getModules().values().forEach(m -> {
                if (m.getKey() == e.getKey()) {
                    m.toggle();
                }
            });
        }
    });

}
