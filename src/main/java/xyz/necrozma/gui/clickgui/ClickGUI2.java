package xyz.necrozma.gui.clickgui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import xyz.necrozma.Client;
import xyz.necrozma.gui.font.CustomFont;
import xyz.necrozma.gui.font.TTFFontRenderer;
import xyz.necrozma.gui.render.RenderUtil;
import xyz.necrozma.module.Category;
import xyz.necrozma.module.Module;
import xyz.necrozma.module.impl.render.ClickGUIModule;
import xyz.necrozma.module.impl.render.PopOutAnimation;
import xyz.necrozma.settings.Settings;
import xyz.necrozma.settings.impl.BooleanSetting;
import xyz.necrozma.settings.impl.NumberSetting;
import xyz.necrozma.util.ConfigState;
import xyz.necrozma.util.InGameBlurUtil;
import xyz.necrozma.util.MathUtil;
import xyz.necrozma.util.TimeUtil;


import java.awt.*;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.time.Year;
import java.util.List;
import java.util.*;


public final class ClickGUI2 extends GuiScreen implements ClickGUIType {

    private float x, y, size;

    //Font
    private static final TTFFontRenderer icon = CustomFont.FONT_MANAGER.getFont("Icon 18");
    private static final TTFFontRenderer icon2 = CustomFont.FONT_MANAGER.getFont("Icon2 18");

    private float width = 320;
    private static float height = 260;

    private float categoryWidth;
    private static float categoryHeight;

    private Category selectedCat = Category.COMBAT;

    private float moduleWidth;
    private float moduleHeight;

    private float offset;
    private float heightOffset;

    private final String separator = File.separator;

    private static float scrollAmount, lastScrollAmount, lastLastScrollAmount;
    private static float renderScrollAmount;

    private final TimeUtil timer = new TimeUtil();
    private final TimeUtil timer2 = new TimeUtil();

    public static NumberSetting selectedSlider;

    public static float firstModulePosition;
    public static float lastModulePosition;

    private boolean holdingGui, resizingGui;
    private float holdingOffsetX, holdingOffsetY;

    private float renderSelectY;

    private boolean hasEditedSliders;

    private List<String> configs;
    private ConfigState configState = ConfigState.NONE;

    public boolean draggingRadar;
    public int oldMouseX;
    public int oldMouseY;

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    Color colorModules;
    Color colorCategory;
    Color colorTop;
    Color selectedCatColor;
    Color booleanColor1;
    Color booleanColor2;
    Color settingColor3;
    Color opacityColor;

    int test;
    int customHue;

//    List<OnlineScriptHandler.OnlineScript> scriptList;
//    private ScriptState scriptState = ScriptState.NONE;

    private final static TTFFontRenderer fontBig = CustomFont.FONT_MANAGER.getFont("Light 19");
    private final static TTFFontRenderer fontLarge = CustomFont.FONT_MANAGER.getFont("Light 24");
    private final static TTFFontRenderer fontExtraLarge = CustomFont.FONT_MANAGER.getFont("Light 36");

    double gap;

    double positionXOfScript;
    double positionYOfScript;
    double widthOfScript;
    double heightOfScript;

    public boolean blockScriptEditorOpen;

    public ClickGUI2() {
        /*try {
            AuthGUI.getClipboardString();
        } catch (final Throwable t) {
            while (5476 < 295728735) {

            }
        }*/
    }

    public void initGui() {
        size = PopOutAnimation.startingSizeValue;

        holdingGui = false;

        resizingGui = false;

        for (final Module m : Client.INSTANCE.getMM().getModules().values()) {
            for (final Settings s : m.getSettings()) {
                if (s instanceof NumberSetting) {
                    final NumberSetting numberSetting = ((NumberSetting) s);
                    numberSetting.renderPercentage = Math.random();
                }
            }
        }

        hasEditedSliders = false;
        blockScriptEditorOpen = false;

        final RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        final List<String> arguments = runtimeMxBean.getInputArguments();

        int i = 0;
        for (final Object s : arguments.toArray()) {
            i++;
            //Rise.addChatMessage(s + " " + i);
        }
    }

    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        GlStateManager.pushMatrix();

        final ScaledResolution sr = new ScaledResolution(mc);

        final boolean canPopUp = Objects.requireNonNull(Client.INSTANCE.getMM().getModule(PopOutAnimation.class)).isToggled() && PopOutAnimation.clickGuiValue;

        if (canPopUp) {
            size = (float) MathUtil.lerp(size, 1, PopOutAnimation.speedValue / (Minecraft.getDebugFPS() / 20D));

            GlStateManager.translate((x - x * size) + (width / 2F - width / 2F * size), (y - y * size) + (height / 2F - height / 2F * size), 0);
            GlStateManager.scale(size, size, 1);
        } else {
            size = 1;
        }

        if (resizingGui) {

            width = 320;
            height = 260;

            while (width < mouseX - x) {
                width += 1;
            }

            while (height < mouseY - y) {
                height += 1;
            }
        }

        categoryWidth = 70;
        categoryHeight = 20;

        colorModules = new Color(39, 42, 48, 255);
        colorCategory = new Color(38, 39, 44, 255);
        colorTop = new Color(39, 42, 49, 255);
        selectedCatColor = new Color(68, 134, 240, 255);
        booleanColor1 = new Color(60, 90, 135, 255);
        booleanColor2 = new Color(68, 134, 240, 255);
        settingColor3 = new Color(70, 100, 145, 255);
        opacityColor = new Color(38, 39, 44, 220);

        //final ModeSetting theme = (ModeSetting) Rise.INSTANCE.getModuleManager().getSetting("ClickGui", "Theme");

        customHue = 0;
        customHue = 265;

        /*
        switch (Objects.requireNonNull(theme).getMode()) {
            case "Rural Amethyst":
                customHue = 265;
                break;

            case "Alyssum Pink":
                customHue = 330;
                break;

            case "Sweet Grape Vine":
                customHue = 130;
                selectedCatColor = new Color(25, 91, 197, 255);
                break;

            case "Orchid Aqua":
                customHue = 200;
                break;

            case "Disco":
                customHue = (test++);
                if (test > 359) test = 0;
                break;
        }



        if (!theme.is("Deep Blue Rise")) {
            colorModules = changeHue(colorModules, customHue / 360f);
            colorCategory = changeHue(colorCategory, customHue / 360f);
            colorTop = changeHue(colorTop, customHue / 360f);
            selectedCatColor = changeHue(selectedCatColor, customHue / 360f);
            booleanColor1 = changeHue(booleanColor1, customHue / 360f);
            booleanColor2 = changeHue(booleanColor2, customHue / 360f);
            settingColor3 = changeHue(settingColor3, customHue / 360f);
            opacityColor = changeHue(opacityColor, customHue / 360f);
        }

         */

        colorModules = changeHue(colorModules, customHue / 360f);
        colorCategory = changeHue(colorCategory, customHue / 360f);
        colorTop = changeHue(colorTop, customHue / 360f);
        selectedCatColor = changeHue(selectedCatColor, customHue / 360f);
        booleanColor1 = changeHue(booleanColor1, customHue / 360f);
        booleanColor2 = changeHue(booleanColor2, customHue / 360f);
        settingColor3 = changeHue(settingColor3, customHue / 360f);
        opacityColor = changeHue(opacityColor, customHue / 360f);

        //if (((BooleanSetting) Objects.requireNonNull(Rise.INSTANCE.getModuleManager().getSetting("ClickGui", "Transparency"))).isEnabled()) {
            //colorCategory = new Color(opacityColor.getRed(), opacityColor.getGreen(), opacityColor.getBlue(), 220);
        //}
        // colorCategory = new Color(opacityColor.getRed(), opacityColor.getGreen(), opacityColor.getBlue(), 220);

        // Background
        RenderUtil.roundedRectCustom(x + categoryWidth, y + categoryHeight, width - categoryWidth, height - categoryHeight, 10, colorModules, false, false, false, true);

        // Category background
        RenderUtil.roundedRectCustom(x, y, categoryWidth, height, 10, colorCategory, true, false, true, false);

        // Above
        RenderUtil.roundedRectCustom(x + categoryWidth, y, width - categoryWidth, categoryHeight, 10, colorTop, false, true, false, false);

        //Logo
        CustomFont.drawStringBig(Client.INSTANCE.getName(), x + 18, y + 0.0, new Color(237, 237, 237).getRGB());
        //CustomFont.drawString(Rise.CLIENT_VERSION, x + 46, y + 3.0, new Color(237, 237, 237).getRGB());

        // Handle the selected category.
        int i = 0;
        for (final Category category : Category.values()) {
            if (category == selectedCat) {
                if (timer2.hasReached(1000 / 120)) {
                    timer2.reset();
                    renderSelectY = MathUtil.lerp(renderSelectY, categoryHeight * (i + 1), 0.15F);
                }

                RenderUtil.rect(x, y + renderSelectY, categoryWidth, categoryHeight, selectedCatColor);
            }

            ++i;
        }

        int amount = 0;
        for (final Category c : Category.values()) {

            final Color color = new Color(237, 237, 237);

            switch (c) {
                case COMBAT: {
                    icon.drawString("a", x + 5, y + categoryHeight * (amount + 1) + categoryHeight / 2 - 3.5F, color.hashCode());
                    break;
                }
                case MOVEMENT: {
                    icon.drawString("b", x + 5, y + categoryHeight * (amount + 1) + categoryHeight / 2 - 3.5F, color.hashCode());
                    break;
                }
                case PLAYER: {
                    icon.drawString("c", x + 5, y + categoryHeight * (amount + 1) + categoryHeight / 2 - 3.5F, color.hashCode());
                    break;
                }
                case RENDER: {
                    icon2.drawString("D", x + 5, y + categoryHeight * (amount + 1) + categoryHeight / 2 - 3.5F, color.hashCode());
                    break;
                }
            }


            CustomFont.drawString(StringUtils.capitalize(c.name().toLowerCase()), x + 18, y + categoryHeight * (amount + 1) + categoryHeight / 2 - 4.5, color.hashCode());

            ++amount;
        }

        GlStateManager.pushMatrix();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        RenderUtil.scissor((x - (categoryWidth - categoryWidth * size)) + categoryWidth, (y + (height / 2F) - (height / 2F) * size) + categoryHeight * size, width - categoryWidth * size, ((height - categoryHeight) * size));

        moduleWidth = width - categoryWidth;
        moduleHeight = 20;
        offset = 5;

        //Modules
        heightOffset = 0;
        firstModulePosition = 9999999;
        amount = 0;

        switch (selectedCat) {
            default:
                for (final Module m : Client.INSTANCE.getMM().getModules().values()) {
                    if (m.isHideInClickGui()) continue;
                    m.sizeInGui = moduleHeight;

                    final Category c = m.getCategory();

                    if (c == selectedCat) {

                        if (firstModulePosition == 9999999)
                            firstModulePosition = categoryHeight + heightOffset + offset * amount + renderScrollAmount;

                        //Settings
                        if (true) { // fix
                            m.sizeInGui = categoryHeight;

                            for (final Settings s : m.getSettings()) {

                                if (!s.isHidden()) {

                                    final float fontWidth = CustomFont.getWidth(s.name) + 5;

                                    final float settingsX = x + categoryWidth + offset + 4;
                                    final float settingsY = y + categoryHeight + heightOffset + +offset * amount + m.sizeInGui + renderScrollAmount;

                                    CustomFont.drawString(s.name, settingsX, settingsY, new Color(237, 237, 237).getRGB());

                                    if (s instanceof BooleanSetting) {
                                        RenderUtil.circle(settingsX + fontWidth, settingsY + 1.5, 7, false, booleanColor1);

                                        if (((BooleanSetting) s).isEnabled()) {
                                            RenderUtil.circle(settingsX + fontWidth + 1.25, settingsY + 1.5 + 1.25, 4.5, true, booleanColor2);
                                        }
                                    }

                                    if (s instanceof NumberSetting) {
                                        final NumberSetting numberSetting = ((NumberSetting) s);

                                        if (selectedSlider == s) {

                                            final double percent = (double) (mouseX - (settingsX + fontWidth)) / (double) (100);
                                            double value = numberSetting.minimum - percent * (numberSetting.minimum - numberSetting.maximum);

                                            if (value > numberSetting.maximum) value = numberSetting.maximum;
                                            if (value < numberSetting.minimum) value = numberSetting.minimum;

                                            numberSetting.value = value;

                                            if (numberSetting.getIncrement() != 0)
                                                numberSetting.value = round(value, (float) numberSetting.increment);
                                            else numberSetting.value = value;

                                            hasEditedSliders = true;
                                        }

                                        numberSetting.percentage = (((NumberSetting) s).value - ((NumberSetting) s).minimum) / (((NumberSetting) s).maximum - ((NumberSetting) s).minimum);

                                        RenderUtil.rect(settingsX + fontWidth, settingsY + 3.5, 100, 2, booleanColor1);
                                        RenderUtil.roundedRect(settingsX + fontWidth + numberSetting.renderPercentage * 100, settingsY + 2, 5, 5, 5, settingColor3);

                                        String value = String.valueOf((float) round(numberSetting.value, (float) numberSetting.increment));

                                        if (numberSetting.increment == 1) {
                                            value = value.replace(".0", "");
                                        }

                                        if (numberSetting.getReplacements() != null) {
                                            for (final String replacement : numberSetting.getReplacements()) {
                                                final String[] split = replacement.split("-");
                                                value = value.replace(split[0], split[1]);
                                            }
                                        }

                                        CustomFont.drawString(value, settingsX + fontWidth + 109, settingsY, new Color(237, 237, 237, 235).hashCode());
                                    }

                                    m.sizeInGui += 12;
                                }
                            }
                        }

                        final float startModuleRenderY = y + categoryHeight;
                        final float moduleRenderY = startModuleRenderY + heightOffset + offset * amount + renderScrollAmount;

                        if (moduleRenderY > startModuleRenderY - m.sizeInGui) {
                            if (moduleRenderY < startModuleRenderY + height) {
                                renderModule(x + categoryWidth + offset, moduleRenderY, moduleWidth - offset * 2, m.sizeInGui, m);
                            }
                        }
                        lastModulePosition = categoryHeight + heightOffset + offset * amount + renderScrollAmount + m.sizeInGui;

                        heightOffset += m.sizeInGui;

                        amount++;

                    }
                }
                break;

        }


        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GlStateManager.popMatrix();

        //Selected category
        final String categoryName = StringUtils.capitalize(selectedCat.name().toLowerCase());
        CustomFont.drawString(categoryName, x + categoryWidth + 3, y + 5.5, new Color(237, 237, 237).getRGB());

        if (timer.hasReached(1000 / 100)) {
            timer.reset();

            for (final Module m : Client.INSTANCE.getMM().getModules().values()) {
                for (final Settings s : m.getSettings()) {
                    if (s instanceof NumberSetting) {
                        final NumberSetting numberSetting = ((NumberSetting) s);

                        if (hasEditedSliders) {
                            numberSetting.renderPercentage = (numberSetting.renderPercentage + numberSetting.percentage) / 2;
                        } else {
                            numberSetting.renderPercentage = (numberSetting.renderPercentage * 4 + numberSetting.percentage) / 5;
                        }

                    }
                }

                //Grey out

                if (!(m.isToggled() || m.isExpanded() && m.settings.size() > 0)) {
                    m.clickGuiOpacity += 6;

                    if (m.clickGuiOpacity > 90) {
                        m.clickGuiOpacity = 90;
                    }
                } else {
                    m.clickGuiOpacity -= 6;

                    if (m.clickGuiOpacity < 1) {
                        m.clickGuiOpacity = 1;
                    }
                }


                heightOffset = 0;
                amount = 0;
                for (final Module module : Client.INSTANCE.getMM().getModules().values()) {

                    if (module.getCategory() == selectedCat) {
                        if (mouseOver(x + categoryWidth, y + categoryHeight + heightOffset + offset * amount + renderScrollAmount, moduleWidth, moduleHeight, mouseX, mouseY)) {
                            module.descOpacityInGui += 0.1;
                        } else {
                            module.descOpacityInGui -= 0.1;
                        }

                        module.descOpacityInGui = Math.max(0, Math.min(255, module.descOpacityInGui));

                        heightOffset += module.sizeInGui;
                        amount++;
                    }
                }
            }

            if (firstModulePosition > categoryHeight && lastModulePosition > height - categoryHeight) {
                scrollAmount *= 0.8;
            }

            if (lastModulePosition < height - categoryHeight && firstModulePosition < categoryHeight) {
                scrollAmount += ((height - categoryHeight) - lastModulePosition) * 0.14;
            }
        }

        renderScrollAmount = lastScrollAmount + (scrollAmount - lastScrollAmount) * mc.timer.renderPartialTicks;

        if (holdingGui) {
            x = mouseX + holdingOffsetX;
            y = mouseY + holdingOffsetY;
        }

        GlStateManager.popMatrix();
    }

//    public void drawScript(final OnlineScriptHandler.OnlineScript script, final double x, final double y, final double width, final double height) {
//        if (script != null) {
//            RenderUtil.roundedRect(x, y, width, height, 9, new Color(255, 255, 255, 10));
//
//            final String scriptName = StringUtils.capitalize(script.getName());
//            final String scriptAuthor = script.getAuthor();
//
//            fontBig.drawString(scriptName, (float) x + 5, (float) y + 5, new Color(237, 237, 237, 237).getRGB());
//            CustomFont.drawString("by " + scriptAuthor, (float) x + 5, (float) y + 16, new Color(237, 237, 237, 177).getRGB());
//
//            RenderUtil.imageCentered(new ResourceLocation("rise/icon/downloadIcon.png"), (int) (x + width - 10), (float) (y + height - 10), 14 / 1.2f, 13 / 1.2f);
//        }
//    }

    public static Color changeHue(Color c, final float hue) {

        // Get saturation and brightness.
        final float[] hsbVals = new float[3];
        Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), hsbVals);

        // Pass .5 (= 180 degrees) as HUE
        c = new Color(Color.HSBtoRGB(hue, hsbVals[1], hsbVals[2]));

        return c;
    }

    public static void updateScroll() {
        lastLastScrollAmount = lastScrollAmount;
        lastScrollAmount = scrollAmount;

        if (lastModulePosition - renderScrollAmount > height - categoryHeight) {

            final float wheel = Mouse.getDWheel();

            scrollAmount += wheel / 10.0F;

            if (wheel == 0) {
                scrollAmount -= (lastLastScrollAmount - scrollAmount) * 0.6;
            }

        } else {
            scrollAmount = 0;
        }
    }

    public void renderModule(final float x, final float y, final float width, final float height, final Module m) {
        //Module background
        RenderUtil.roundedRect(x, y, width, height, 5, new Color(255, 255, 255, 10));

        //Module name
        CustomFont.drawString(m.getName(), x + 4, y + 6, ((m.isToggled()) ? booleanColor2 : new Color(237, 237, 237)).getRGB());

        //Switch
        if (!m.getName().equals("Interface")) {
            RenderUtil.roundedRect(x + width - 15, y + 8, 10, 5, 5, new Color(255, 255, 255, 255));
            RenderUtil.circle(x + width - ((m.isToggled()) ? 10 : 17), y + 7, 7, booleanColor1);
        }

        if (m.clickGuiOpacity != 1)
            RenderUtil.roundedRect(x, y, width, height, 5, new Color(39, 42, 48, Math.round(m.clickGuiOpacity)));

        //Module description
        if (m.descOpacityInGui > 1)
            CustomFont.drawStringSmall(m.getDescription(), x + (CustomFont.getWidth(m.getName())) + 6, y + 8, new Color(175, 175, 175, Math.round(m.descOpacityInGui)).getRGB());

    }

//    public void renderScript(final float x, final float y, final float width, final float height, final Script script) {
//        //Module background
//        RenderUtil.roundedRect(x, y, width, height, 5, new Color(255, 255, 255, 10));
//
//        //Module name
//        CustomFont.drawString(script.getName(), x + 4, y + 6, ((script.isEnabled()) ? booleanColor2 : new Color(237, 237, 237)).getRGB());
//
//        //Switch
//        RenderUtil.roundedRect(x + width - 15, y + 8, 10, 5, 5, new Color(255, 255, 255, 255));
//        RenderUtil.circle(x + width - ((script.isEnabled()) ? 10 : 17), y + 7, 7, booleanColor1);
//
//        if (script.clickGuiOpacity != 1)
//            RenderUtil.roundedRect(x, y, width, height, 5, new Color(39, 42, 48, Math.round(script.clickGuiOpacity)));
//    }

    public void renderModule(final float x, final float y, final float width, final float height, final String n) {
        //Module background
        RenderUtil.roundedRect(x, y, width, height, 5, new Color(255, 255, 255, 10));

        //Module name
        CustomFont.drawString(n, x + 4, y + 6, booleanColor2.hashCode());
    }

    public void mouseClicked(final int mouseX, final int mouseY, final int button) {
        final int grabSize = 10;

//        if (blockScriptEditorOpen) {
//            GuiBlockScript.mouseClicked(mouseX, mouseY, button);
//            return;
//        }

        if (mouseOver(x + width - grabSize + 3, y + height - grabSize + 3, grabSize, grabSize, mouseX, mouseY)) {
            resizingGui = true;
            return;
        }

        int amount = 0;
        for (final Category c : Category.values()) {
            if (mouseOver(x, y + categoryHeight * (amount + 1), categoryWidth, categoryHeight, mouseX, mouseY) && selectedCat != c) {

//                if (c == Category.STORE) {
//                    scriptState = ScriptState.NONE;
//                    scriptList = null;
//
//                    if (button == 1) {
//                        blockScriptEditorOpen = true;
//                        GuiBlockScript.onInit();
//                    }
//                }

                selectedCat = c;

                scrollAmount = 0;
                renderScrollAmount = 0;
                lastScrollAmount = -30;
                lastLastScrollAmount = -30;

                scrollAmount = 0;

                mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));

                for (final Module m : Client.INSTANCE.getMM().getModules().values()) {
                    for (final Settings s : m.getSettings()) {
                        if (s instanceof NumberSetting) {
                            final NumberSetting numberSetting = ((NumberSetting) s);
                            numberSetting.renderPercentage = Math.random();
                        }
                    }
                }

            }

            ++amount;
        }

        //Modules
        heightOffset = 0;
        amount = 0;

        if (mouseOver(x + categoryWidth, y + categoryHeight, width - categoryWidth, height - categoryHeight, mouseX, mouseY)) {
            for (final Module m : Client.INSTANCE.getMM().getModules().values()) {
                if (m.isHideInClickGui()) continue;
                m.sizeInGui = moduleHeight;

                if (m.expanded) {
                    m.sizeInGui = categoryHeight;

                    for (final Settings ignored : m.getSettings()) {
                        m.sizeInGui += 12;
                    }
                }

                if (m.getCategory() == selectedCat) {
                    if (m.expanded) {
                        m.sizeInGui = categoryHeight;

                        for (final Settings s : m.getSettings()) {

                            if (!s.isHidden()) {

                                final float settingsX = x + categoryWidth + offset + 4;
                                final float settingsY = y + categoryHeight + heightOffset + offset * amount + m.sizeInGui + renderScrollAmount;

                                if (mouseOver(settingsX, settingsY, width - categoryWidth - offset * 3, 11, mouseX, mouseY)) {
                                    if (s instanceof BooleanSetting) {
                                        if (button == 0) ((BooleanSetting) s).toggle();
                                    }
                                    if (s instanceof NumberSetting) {
                                        if (button == 0) selectedSlider = (NumberSetting) s;
                                    }
                                }

                                m.sizeInGui += 12;
                            }
                        }
                    }

                    if (mouseOver(x + categoryWidth, y + categoryHeight + heightOffset + offset * amount + renderScrollAmount, moduleWidth, moduleHeight, mouseX, mouseY)) {
                        if (button == 0 && !m.getName().equals("ClickGui")) {
                            m.toggle();
                            mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
                        }
                        if (button == 1) {
                            m.expanded = !m.expanded;
                        }
                    }

                    heightOffset += m.sizeInGui;

                    amount++;
                }
            }
        }

        if (mouseOver(x, y, width, categoryHeight, mouseX, mouseY)) {
            holdingGui = true;
            holdingOffsetX = x - mouseX;
            holdingOffsetY = y - mouseY;
        }

    }

    protected void mouseReleased(final int mouseX, final int mouseY, final int state) {
        selectedSlider = null;
        holdingGui = resizingGui = false;
        draggingRadar = false;
        //if (blockScriptEditorOpen) GuiBlockScript.releasedMouseButton();
        super.mouseReleased(mouseX, mouseY, state);
    }

    public boolean mouseOver(final float posX, final float posY, final float width, final float height, final float mouseX, final float mouseY) {
        return mouseX > posX && mouseX < posX + width && mouseY > posY && mouseY < posY + height;
    }

    public void onGuiClosed() {
        selectedSlider = null;
        holdingGui = resizingGui = false;
        Objects.requireNonNull(Client.INSTANCE.getMM().getModule(ClickGUIModule.class)).toggle();
    }

//    private static double round(final double value, final int places) {
//        if (places < 0) throw new IllegalArgumentException();
//
//        BigDecimal bd = new BigDecimal(Double.toString(value));
//        bd = bd.setScale(places, RoundingMode.HALF_UP);
//        return bd.doubleValue();
//    }

    private static double round(final double value, final float places) {
        if (places < 0) throw new IllegalArgumentException();

        final double precision = 1 / places;
        return Math.round(value * precision) / precision;
    }

    private long days(final String date) {
        // creating the date 1 with sample input date.
        final Date date1 = new Date(Year.now().getValue(), Calendar.getInstance().get(Calendar.MONTH) + 1, Calendar.getInstance().get(Calendar.DATE));

        // creating the date 2 with sample input date.
        final String[] split = date.split("/");

        final Date date2 = new Date(Integer.parseInt(split[2]), Integer.parseInt(split[1]), Integer.parseInt(split[0]));

        // getting milliseconds for both dates
        final long date1InMs = date1.getTime();
        final long date2InMs = date2.getTime();

        // getting the diff between two dates.
        long timeDiff = 0;
        if (date1InMs > date2InMs) {
            timeDiff = date1InMs - date2InMs;
        } else {
            timeDiff = date2InMs - date1InMs;
        }

        // print diff in days
        return (int) (timeDiff / (1000 * 60 * 60 * 24));
    }
}
