package xyz.necrozma.module;

import lombok.Getter;
import lombok.Setter;
import me.zero.alpine.listener.Subscriber;
import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.Validate;
import xyz.necrozma.Client;
import xyz.necrozma.event.impl.input.MoveButtonEvent;
import xyz.necrozma.event.impl.motion.BlockCollideEvent;
import xyz.necrozma.event.impl.motion.MoveEvent;
import xyz.necrozma.event.impl.motion.PreMotionEvent;
import xyz.necrozma.event.impl.motion.StrafeEvent;
import xyz.necrozma.event.impl.packet.EventPacket;
// import xyz.necrozma.event.impl.packet.PacketSendEvent;
import xyz.necrozma.event.impl.packet.PacketReceiveEvent;
import xyz.necrozma.event.impl.render.Render2DEvent;
import xyz.necrozma.event.impl.render.Render3DEvent;
import xyz.necrozma.event.impl.update.EventUpdate;
import xyz.necrozma.settings.Settings;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public abstract class Module implements Subscriber {

    @Setter
    private boolean hideInClickGui;

    private boolean toggled;

    public float sizeInGui;

    private final String name;

    private final String description;

    private final Category category;

    public boolean expanded;
    public float clickGuiOpacity;
    public float descOpacityInGui = 1;

    @Setter
    private int misc;

    public List<Settings> settings = new ArrayList<>();

    @Setter
    private int key;

    protected final Minecraft mc = Client.INSTANCE.getMC();

    public Module() {
        ModuleInfo info = getClass().getAnnotation(ModuleInfo.class);
        Validate.notNull(info, "Bad annotation");

        this.name = info.name();
        this.description = info.description();
        this.category = info.category();
    }

    public void onEnable() {
        Client.BUS.subscribe(this);
    }

    public void onDisable() {
        Client.BUS.unsubscribe(this);
    }

    public void onToggle() {}


    public void toggle() {
        onToggle();
        if (toggled) {
            toggled = false;
            onDisable();
        } else {
            toggled = true;
            onEnable();
        }
    }

    public void setToggled(boolean toggled) {
        onToggle();
        if(toggled) {
            this.toggled = false;
            onDisable();
        } else {
            this.toggled = true;
            onEnable();
        }
    }

    public Settings getSetting(final String name) {
        for (final Settings setting : settings) {
            if (setting.name.equalsIgnoreCase(name)) {
                return setting;
            }
        }

        return null;
    }

    public Settings getSettingAlternative(final String name) {
        for (final Settings setting : settings) {
            final String comparingName = setting.name.replaceAll(" ", "");

            if (comparingName.equalsIgnoreCase(name)) {
                return setting;
            }
        }

        return null;
    }

    public void onPreMotion(final PreMotionEvent event) {
    }

    public void onRender3DEvent(Render3DEvent event) {
    }

    public void onRender2DEvent(Render2DEvent event) {
    }

    public void onUpdateAlwaysInGui() {
    }

    public void onPacketSend(final EventPacket event) {
    }
    public void onUpdate(final EventUpdate event) {
    }
    public void onBlockCollide(final BlockCollideEvent event) {
    }
    public void onMove(final MoveEvent event) {
    }
    public void onUpdateAlways() {
    }

    public void onMoveButton(final MoveButtonEvent event) {
    }

    public void onStrafe(final StrafeEvent event) {
    }

    public void onPacketReceive(final PacketReceiveEvent event) {
    }

    protected double randomDouble(final double min, final double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    protected int randomInt(final int min, final int max) {
        return ThreadLocalRandom.current().nextInt(min, max);
    }

    protected double random() {
        return ThreadLocalRandom.current().nextDouble(1);
    }
}
