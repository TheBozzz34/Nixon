package xyz.necrozma.module.impl.render;

/*
 Copyright Alan Wood 2021
 None of this code to be reused without my written permission
 Intellectual Rights owned by Alan Wood
 */


import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;
import xyz.necrozma.Client;
import xyz.necrozma.event.impl.motion.PreMotionEvent;
import xyz.necrozma.event.impl.render.Render2DEvent;
import xyz.necrozma.module.Category;
import xyz.necrozma.module.Module;
import xyz.necrozma.module.ModuleInfo;
import xyz.necrozma.settings.impl.BooleanSetting;
import xyz.necrozma.settings.impl.ModeSetting;
import xyz.necrozma.settings.impl.NumberSetting;

import java.util.Objects;


@ModuleInfo(name = "ClickGui", description = "Opens a Gui where you can toggle modules and change their settings", category = Category.RENDER)
public final class ClickGui extends Module {

    private final ModeSetting mode = new ModeSetting("Mode", this, "Rise", "Rise", "Dropdown");

    private final ModeSetting theme = new ModeSetting("Theme", this, "Deep Blue Rise", "Deep Blue Rise",
            "Rural Amethyst", "Rustic Desert", "Orchid Aqua", "Alyssum Pink", "Sweet Grape Vine", "Disco");
    private final BooleanSetting transparency = new BooleanSetting("Transparency", this, false);
    private final BooleanSetting blur = new BooleanSetting("Blur Background", this, false);
    private final NumberSetting scale = new NumberSetting("Scale", this, 0.7, 0.3, 1, 0.05);

    private final KeyBinding[] affectedBindings = new KeyBinding[]{
            mc.gameSettings.keyBindForward,
            mc.gameSettings.keyBindBack,
            mc.gameSettings.keyBindRight,
            mc.gameSettings.keyBindLeft,
            mc.gameSettings.keyBindJump
    };

    public static Boolean brickClickGUI = null;

    public ClickGui() {
        setKey(Keyboard.KEY_GRAVE);
        setHideInClickGui(true);
    }

    @Override
    public void onUpdateAlwaysInGui() {
        transparency.hidden = !mode.is("Rise");
        blur.hidden = !mode.is("Rise");

        scale.hidden = !mode.is("Dropdown");
    }

    @Override
    public void onEnable() {
        /*
        switch (mode.getMode()) {
            case "Rise": {
                mc.displayGuiScreen(Client.INSTANCE.getClickGUI());
                break;
            }

            case "Dropdown": {
                mc.displayGuiScreen(Rise.INSTANCE.getStrikeGUI());
                break;
            }
        }

         */
        mc.displayGuiScreen(Client.INSTANCE.getStrikeGUI());

        //Rise.INSTANCE.getExecutorService().execute(Rise.INSTANCE::saveClient);
    }

    @Override
    public void onDisable() {
        //Rise.INSTANCE.getExecutorService().execute(Rise.INSTANCE::saveClient);
    }

    /*
    @Override
    public void onPreMotion(final PreMotionEvent event) {
        //Invmove for clickgui
        if (!(mc.currentScreen instanceof GuiChat)
                && !Objects.requireNonNull(Rise.INSTANCE.getModuleManager().getModule("InvMove")).isEnabled()) {
            for (final KeyBinding a : affectedBindings) {
                a.setKeyPressed(GameSettings.isKeyDown(a));
            }
        }
    }

     */

    /*
    @Override
    public void onUpdate(final UpdateEvent event) {
        if (mc.currentScreen == Rise.INSTANCE.getClickGUI()) ClickGUI.updateScroll();
    }

     */




}