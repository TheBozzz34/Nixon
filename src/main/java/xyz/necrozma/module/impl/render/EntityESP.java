package xyz.necrozma.module.impl.render;

import me.zero.alpine.listener.Listener;
import me.zero.alpine.listener.Subscribe;
import org.lwjgl.input.Keyboard;
import xyz.necrozma.event.impl.update.EventUpdate;
import xyz.necrozma.module.Category;
import xyz.necrozma.module.Module;
import xyz.necrozma.module.ModuleInfo;
import xyz.necrozma.settings.impl.NumberSetting;

@ModuleInfo(name = "Entity ESP", description = "Highlights entities", category = Category.RENDER)
public final class EntityESP extends Module {

    private final NumberSetting gamma = new NumberSetting("Gamma", this, 500, 100, 700, 50);

    public EntityESP() {
        setKey(Keyboard.KEY_J);
    }
    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}

