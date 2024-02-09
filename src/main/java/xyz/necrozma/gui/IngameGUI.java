package xyz.necrozma.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;



import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Keyboard;
import xyz.necrozma.Client;
import xyz.necrozma.gui.font.CustomFont;
import xyz.necrozma.gui.font.TTFFontRenderer;
import xyz.necrozma.gui.render.KeystrokeUtil;
import xyz.necrozma.gui.render.RenderUtil;
import xyz.necrozma.module.Category;
import xyz.necrozma.module.Module;
import xyz.necrozma.module.impl.render.ClickGUIModule;
import xyz.necrozma.settings.impl.BooleanSetting;
import xyz.necrozma.util.MathUtil;
import xyz.necrozma.util.TimeUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * This class is used to render in-game gui.
 * <p>
 * This class will extend of the default in-game gui
 * yet will have more features as we add our own to the already existing gui.
 *
 * @author Tecnio
 * @since 02/12/2021
 */


public final class IngameGUI extends GuiIngame {

    public static final KeystrokeUtil forward = new KeystrokeUtil();
    public static final KeystrokeUtil backward = new KeystrokeUtil();
    public static final KeystrokeUtil left = new KeystrokeUtil();
    public static final KeystrokeUtil right = new KeystrokeUtil();
    public static final KeystrokeUtil space = new KeystrokeUtil();

    private static List<Object> modules;
    private final static TTFFontRenderer comfortaa = CustomFont.FONT_MANAGER.getFont("Comfortaa 18");
    private final static TTFFontRenderer comfortaaBig = CustomFont.FONT_MANAGER.getFont("Comfortaa 32");
    private final static TTFFontRenderer skeet = CustomFont.FONT_MANAGER.getFont("SkeetBold 12");
    private final static TTFFontRenderer skeetBig = CustomFont.FONT_MANAGER.getFont("Skeet 18");
    private final static TTFFontRenderer oneTap = CustomFont.FONT_MANAGER.getFont("Skeet 16");
    private final static TTFFontRenderer museo = CustomFont.FONT_MANAGER.getFont("Museo 20");
    private final static TTFFontRenderer eaves = CustomFont.FONT_MANAGER.getFont("Eaves 18");

    private final Minecraft mc = Minecraft.getMinecraft();
    public IngameGUI(final Minecraft mcIn) {
        super(mcIn);
    }

    public static void renderKeyStrokes() {

        final Minecraft mc = Minecraft.getMinecraft();
        final ScaledResolution SR = new ScaledResolution(mc);

        //final float xPercentage = (float) ((NumberSetting) Objects.requireNonNull(Rise.INSTANCE.getModuleManager().getSetting("Interface", "KeystrokesX"))).getValue();
        //final float yPercentage = (float) ((NumberSetting) Objects.requireNonNull(Rise.INSTANCE.getModuleManager().getSetting("Interface", "KeystrokesY"))).getValue();

        final float xPercentage = 2;
        final float yPercentage = 2;
        final int x = (int) (SR.getScaledWidth() * xPercentage) / 100;
        final int y = (int) (SR.getScaledHeight() * yPercentage) / 100;

        final int distanceBetweenButtons = 34;
        final int width = 30;

        forward.setUpKey(mc.gameSettings.keyBindForward);
        forward.updateAnimations();
        forward.drawButton(x, y, width);

        backward.setUpKey(mc.gameSettings.keyBindBack);
        backward.updateAnimations();
        backward.drawButton(x, y + distanceBetweenButtons, width);

        left.setUpKey(mc.gameSettings.keyBindLeft);
        left.updateAnimations();
        left.drawButton(x - distanceBetweenButtons, y + distanceBetweenButtons, width);

        right.setUpKey(mc.gameSettings.keyBindRight);
        right.updateAnimations();
        right.drawButton(x + distanceBetweenButtons, y + distanceBetweenButtons, width);

        space.setUpKey(mc.gameSettings.keyBindJump);
        space.updateAnimations();
        space.drawButton(x, y + distanceBetweenButtons * 2, width);
    }

    private void renderModules() {

        AtomicInteger yOffSet = new AtomicInteger(4);
        int yOffsetInc = 10;
        int squareSize = 2;
        ScaledResolution scaledResolution = new ScaledResolution(mc);

        int centerX = scaledResolution.getScaledWidth() / 2;
        int centerY = scaledResolution.getScaledHeight() / 2;

        Client.INSTANCE.getMM().getModules().values().forEach(module -> {
            int width = mc.fontRendererObj.getStringWidth(module.getName()) + 2;
            if (module.isToggled()) {
                // RenderUtil.roundedRect(2, yOffSet.get() - 2, width + 2, 10, 2, new Color(0, 0, 0, 55 + 0));
                CustomFont.drawString(module.getName(), 2, yOffSet.get(), 0xFF00FF00);
                CustomFont.drawString("[" + Keyboard.getKeyName(module.getKey()) + "]", width + 2, yOffSet.get(), 0xFF00FFFF);
                yOffSet.addAndGet(yOffsetInc);
            }
        });
    }

    private void renderLocation() {
        final Vec3 vec = mc.thePlayer.getPositionVector();
        final String location = "X: " + (int) vec.xCoord + " Y: " + (int) vec.yCoord + " Z: " + (int) vec.zCoord;
        skeetBig.drawStringWithShadow(location, 2, new ScaledResolution(mc).getScaledHeight() - 10, 0xFFFFFFFF);
    }

    private void renderBPS() {
        final ScaledResolution sr = new ScaledResolution(mc);

        final double x = 2, y = sr.getScaledHeight() - 20;
        final String bps = "BPS: " + MathUtil.round(((Math.hypot(mc.thePlayer.posX - mc.thePlayer.prevPosX, mc.thePlayer.posZ - mc.thePlayer.prevPosZ) * mc.timer.timerSpeed) * 20), 2);

        skeetBig.drawStringWithShadow(bps, (float) x, (float) y, 0xFFFFFFFF);

    }

    private void drawArmorHud() {
        ScaledResolution sr = new ScaledResolution(mc);

        int width = sr.getScaledWidth();
        int height = sr.getScaledHeight();

        int i = width / 2;
        int j = height - 55;

        RenderUtil.drawArmorHUD(i, j);



    }

    private int lastColor = 0;
    private int colorCounter = 0;
    private int randomColor() {
        if (colorCounter == 0) {
            lastColor = new Color((int) (Math.random() * 0x1000000)).getRGB();
            colorCounter = 10;
        }
        colorCounter--;
        return lastColor;
    }

    private void renderClientName() {
        CustomFont.drawStringBigWithDropShadow("Nixon", 2, 5, new Color(159, 24, 242).hashCode());

        int offset = (int) (CustomFont.getWidthBig("Nixon") + 2);
        CustomFont.drawStringWithDropShadow("1.0.0", 1 + offset, 5, new Color(159, 24, 242).hashCode());

        }

    @Override
    public void renderGameOverlay(final float partialTicks) {

        super.renderGameOverlay(partialTicks);

        /*
         * For some GUI stuff we don't want to render while F3 menu is enabled so we check for it.
         * For other GUI stuff that we want to run while F3 is enabled, well we can just call the rendering regardless.
         */
        if (!mc.gameSettings.showDebugInfo && !Client.INSTANCE.getMM().getModule(ClickGUIModule.class).isToggled()) {
            renderClientName();
            renderArrayList();
            //renderKeyStrokes();
            renderLocation();
            drawArmorHud();
            renderBPS();
        }

    }

    private void renderArrayList() {
        // final String mode = this.getMode(Interface.class, "Theme");
        final String mode = "Rise";

        final ScaledResolution SR = new ScaledResolution(mc);

        final float offset = 6;

        final float arraylistX = SR.getScaledWidth() - offset;

        modules = new ArrayList<>();

        int yOffSet = 5;

        // modules.addAll(Client.INSTANCE.getMM().getModules().values());

        for (Module module : Client.INSTANCE.getMM().getModules().values()) {
            if (module.isToggled()) {
                modules.add(module);
            }
        }

        modules.sort(Comparator.comparingInt(module -> -mc.fontRendererObj.getStringWidth(((Module) module).getName())));

        for (Object module : modules) {
            final int offsetY = 2;
            final int offsetX = 1;

            final String name = module instanceof Module ? ((Module) module).getName() : "null";

            float finalX = 0;

            final float renderX = arraylistX - comfortaa.getWidth(name);
            final float renderY = yOffSet;

            final double stringWidth = comfortaa.getWidth(name);
            RenderUtil.rect(renderX - offsetX, renderY - offsetY + 1 + 0.1, stringWidth + offsetX * 1.5 + 0.5, comfortaa.getHeight() + offsetY + 0.3, new Color(0, 0, 0, 80));

            finalX = arraylistX - comfortaa.getWidth(name);

            comfortaa.drawStringWithShadow(name, renderX + 0.5f, renderY + 1, 0xFFFFFFFF);

            yOffSet += comfortaa.getHeight() + 1;
        }


    }



}