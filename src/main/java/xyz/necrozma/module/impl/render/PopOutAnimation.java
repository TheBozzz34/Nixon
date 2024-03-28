package xyz.necrozma.module.impl.render;


import xyz.necrozma.module.Category;
import xyz.necrozma.module.Module;
import xyz.necrozma.module.ModuleInfo;
import xyz.necrozma.settings.impl.BooleanSetting;
import xyz.necrozma.settings.impl.NumberSetting;

@ModuleInfo(name = "PopOutAnimation", description = "Gives Guis an animation", category = Category.RENDER)
public class PopOutAnimation extends Module {
    private final BooleanSetting clickGui = new BooleanSetting("ClickGui", this, true);
    public static boolean clickGuiValue;
    private final BooleanSetting inventories = new BooleanSetting("Inventories", this, true);
    public static boolean inventoriesValue;

    private final NumberSetting startingSize = new NumberSetting("Starting Size", this, 0.1, 0, 1, 0.01);
    public static float startingSizeValue;
    private final NumberSetting speed = new NumberSetting("Speed", this, 0.1, 0.01, 1, 0.01);
    public static float speedValue;

    @Override
    public void onUpdateAlwaysInGui() {
        super.onUpdateAlwaysInGui();

        clickGuiValue = clickGui.isEnabled();
        inventoriesValue = inventories.isEnabled();
        startingSizeValue = (float) startingSize.getValue();
        speedValue = (float) speed.getValue();
    }
}
