package xyz.necrozma.module;

import lombok.Getter;
import lombok.Setter;
import me.zero.alpine.listener.Subscriber;
import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.Validate;
import org.lwjgl.input.Keyboard;
import xyz.necrozma.Client;
import xyz.necrozma.settings.Settings;

import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class Module implements Subscriber {

    private boolean toggled;

    private final String name;

    private final String description;

    private final Category category;

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
}
