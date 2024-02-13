package xyz.necrozma.module.impl.render;

import me.zero.alpine.listener.Listener;
import me.zero.alpine.listener.Subscribe;
import org.lwjgl.input.Keyboard;
import xyz.necrozma.event.impl.update.EventUpdate;
import xyz.necrozma.module.Category;
import xyz.necrozma.module.Module;
import xyz.necrozma.module.ModuleInfo;
import xyz.necrozma.settings.impl.NumberSetting;
import xyz.necrozma.util.ChatUtil;

@ModuleInfo(name = "Fullbright", description = "Makes everything brighter", category = Category.RENDER)
public final class Fullbright extends Module {

    private final NumberSetting gamma = new NumberSetting("Gamma", this, 500, 100, 700, 50);

    private float oldGamma;

    public Fullbright() {

    }
    @Override
    public void onEnable() {
        super.onEnable();
        oldGamma = mc.gameSettings.gammaSetting;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        mc.gameSettings.gammaSetting = oldGamma;

    }

    @Subscribe
    private final Listener<EventUpdate> listener = new Listener<>(e -> {
        if (mc.thePlayer != null) {
            mc.gameSettings.gammaSetting = (float) gamma.getValue() / 100;
        }
    });
}
