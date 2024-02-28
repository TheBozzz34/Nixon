package xyz.necrozma.module.impl.render;

import org.lwjgl.input.Keyboard;
import xyz.necrozma.Client;
import xyz.necrozma.module.Category;
import xyz.necrozma.module.Module;
import xyz.necrozma.module.ModuleInfo;
import xyz.necrozma.settings.impl.NumberSetting;

@ModuleInfo(name = "ClickGUI", description = "Displays the ClickGui", category = Category.RENDER)
public class ClickGUIModule extends Module {

    private final NumberSetting gamma = new NumberSetting("Gamma", this, 500, 100, 700, 50);

    public ClickGUIModule() {

        setKey(Keyboard.KEY_GRAVE);
        setHideInClickGui(true);
    }
    @Override
    public void onEnable() {
        super.onEnable();

        mc.displayGuiScreen(Client.INSTANCE.getClickGUI());
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}


