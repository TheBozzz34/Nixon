package xyz.necrozma;


import lombok.Setter;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Session;
import net.minecraft.util.Vec3;
import xyz.necrozma.auth.AuthenticationService;
import xyz.necrozma.discord.IPCClient;
import xyz.necrozma.discord.IPCListener;
import xyz.necrozma.discord.entities.RichPresence;
import xyz.necrozma.exception.CommandException;
import xyz.necrozma.gui.clickgui.ClickGUI2;
import xyz.necrozma.gui.render.RenderUtil;
import xyz.necrozma.login.AuthenticationException;
import xyz.necrozma.login.AuthenticationResult;
import xyz.necrozma.login.XboxLiveMojangAuth;
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
import xyz.necrozma.login.WebLoginHelper;


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
    private ConfigManager configManager;
    private AuthenticationService authService;

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

        /*
        try {
            System.out.println("Starting authentication process...");

            AuthTokens tokens = WebLoginHelper.getTokensFromWebLogin();

            // Debug the tokens
            System.out.println("Got tokens from web login:");
            System.out.println("Access token: " + (tokens.getAccessToken() != null ?
                    tokens.getAccessToken().substring(0, Math.min(50, tokens.getAccessToken().length())) + "..." : "null"));
            System.out.println("Refresh token: " + (tokens.getRefreshToken() != null ? "Present" : "null"));

            if (tokens.getAccessToken() == null || tokens.getAccessToken().trim().isEmpty()) {
                throw new IOException("Access token is null or empty");
            }

            System.out.println("Starting Xbox Live authentication...");

            try {
                XboxLiveMojangAuth auth = new XboxLiveMojangAuth();
                AuthenticationResult result = auth.authenticate(tokens.getAccessToken());

                System.out.println("Authentication successful!");
                System.out.println("Xbox Username: " + result.getUsername());
                System.out.println("Minecraft Username: " + result.getMinecraftUsername());
                System.out.println("Minecraft UUID: " + result.getUuid());
                System.out.println("Token expires in: " + result.getExpiresIn() + " seconds");

                // For your Minecraft session constructor:
                String usernameIn = result.getMinecraftUsername(); // Minecraft username
                String playerIDIn = result.getUuid();              // Minecraft UUID (this is what you need!)
                String tokenIn = result.getAccessToken();          // Bearer token
                String sessionTypeIn = "legacy";                   // or whatever session type you use

                if (usernameIn == null || playerIDIn == null || tokenIn == null) {
                    throw new IOException("Authentication result contains null values");
                }

                System.out.println("Creating Minecraft session...");
                Minecraft.getMinecraft().setSession(new Session(
                        usernameIn,
                        playerIDIn,
                        tokenIn,
                        sessionTypeIn));

                System.out.println("Session created successfully!");

            } catch (AuthenticationException e) {
                System.err.println("Xbox Live authentication failed: " + e.getMessage());
                e.printStackTrace();

                // Print the full stack trace to understand what went wrong
                if (e.getCause() != null) {
                    System.err.println("Caused by: " + e.getCause().getClass().getSimpleName() + ": " + e.getCause().getMessage());
                }

                throw new IOException("Xbox Live authentication failed: " + e.getMessage(), e);
            }

        } catch (Exception e) {
            System.err.println("An unexpected error occurred during authentication: " + e.getMessage());
            e.printStackTrace();

            // Print detailed error information
            System.err.println("Error type: " + e.getClass().getSimpleName());
            if (e.getCause() != null) {
                System.err.println("Root cause: " + e.getCause().getClass().getSimpleName() + ": " + e.getCause().getMessage());
            }

            throw new IOException("Unexpected error during authentication", e);
        }

         */



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


        //clickGUI = new ClickGUI();
        clickGUI = new ClickGUI();
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

    private void authenticateUser() {
        System.out.println("Starting authentication process...");

        if (authService.authenticate()) {
            System.out.println("Authentication successful!");

            final AuthenticationResult result = authService.getLastAuthResult();
            if (result != null) {
                String usernameIn = result.getMinecraftUsername(); // Minecraft username
                String playerIDIn = result.getUuid();              // Minecraft UUID (this is what you need!)
                String tokenIn = result.getAccessToken();          // Bearer token
                String sessionTypeIn = "legacy";                   // or whatever session type you use

                if (usernameIn == null || playerIDIn == null || tokenIn == null) {
                    System.err.println("Authentication result contains null values");
                    return;
                }

                System.out.println("Creating Minecraft session...");
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
                    // NM.registerNotification(m.getName() + " toggled!", 3000, NotificationType.NOTIFICATION);
                }
            });
        }
    });
}
