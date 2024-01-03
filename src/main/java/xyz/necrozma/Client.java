package xyz.necrozma;

import lombok.Getter;
import me.zero.alpine.bus.EventBus;
import me.zero.alpine.bus.EventManager;
import me.zero.alpine.listener.Listener;
import me.zero.alpine.listener.Subscribe;
import me.zero.alpine.listener.Subscriber;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import xyz.necrozma.event.impl.input.EventKey;
import xyz.necrozma.module.ModuleManager;
import xyz.necrozma.command.CommandManager;
import xyz.necrozma.util.ChatUtil;

@Getter
public enum Client implements Subscriber {
    INSTANCE;

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
    });
}
