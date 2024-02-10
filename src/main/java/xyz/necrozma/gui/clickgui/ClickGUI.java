package xyz.necrozma.gui.clickgui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import xyz.necrozma.Client;
import xyz.necrozma.gui.font.CustomFont;
import xyz.necrozma.gui.font.TTFFontRenderer;
import xyz.necrozma.gui.render.RenderUtil;
import xyz.necrozma.module.Category;
import xyz.necrozma.module.Module;
import xyz.necrozma.module.impl.render.ClickGUIModule;
import xyz.necrozma.settings.impl.BooleanSetting;
import xyz.necrozma.settings.impl.NumberSetting;
import xyz.necrozma.util.MathUtil;
import xyz.necrozma.util.TimeUtil;
import org.apache.commons.lang3.StringUtils;
import xyz.necrozma.settings.Settings;

import java.awt.*;
import java.io.IOException;
import java.time.Year;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;



public final class ClickGUI extends GuiScreen implements ClickGUIType {

    private float x, y, size;

    private final Color booleanColor1 = new Color(60, 90, 135, 255);
    private final Color booleanColor2 = new Color(68, 134, 240, 255);

    private boolean hasEditedSliders;

    private final TTFFontRenderer icon = CustomFont.FONT_MANAGER.getFont("Icon 18");
    private final TTFFontRenderer icon2 = CustomFont.FONT_MANAGER.getFont("Icon2 18");

    private Category selectedCat = Category.RENDER;

    private final TimeUtil timer = new TimeUtil();
    private final TimeUtil timer2 = new TimeUtil();

    private float moduleWidth;
    private float moduleHeight;

    private float offset;
    private float heightOffset;

    public float firstModulePosition;
    public float lastModulePosition;

    private float scrollAmount, lastScrollAmount, lastLastScrollAmount;
    private float renderScrollAmount;

    public static NumberSetting selectedSlider;

    private float renderSelectY;

    private int categoryWidth = 70;
    private int categoryHeight = 20;
    private Color colorCategory = new Color(38, 39, 44, 255);
    private Color colorTop = new Color(39, 42, 49, 255);
    private Color colorModules = new Color(39, 42, 48, 255);
    private Color selectedCatColor = new Color(68, 134, 240, 255);
    private Color settingColor3 = new Color(70, 100, 145, 255);

    public ClickGUI() {

    }

    public void initGui() {
        /*
        size = PopOutAnimation.startingSizeValue;

        holdingGui = false;

        resizingGui = false;

        for (final Module m : Rise.INSTANCE.getModuleManager().getModuleList()) {
            for (final Setting s : m.getSettings()) {
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

         */
    }

    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {

        /*
        x = 50;
        y = 50;

         */
        // Background
        RenderUtil.roundedRectCustom(x + categoryWidth, y + categoryHeight, width - categoryWidth, height - categoryHeight, 10, colorModules, false, false, false, true);

        // Category background
        RenderUtil.roundedRectCustom(x, y, categoryWidth, height, 10, colorCategory, true, false, true, false);

        // Above
        RenderUtil.roundedRectCustom(x + categoryWidth, y, width - categoryWidth, categoryHeight, 10, colorTop, false, true, false, false);

        //Logo
        CustomFont.drawStringBig(Client.INSTANCE.getName(), x + 18, y + 0.0, new Color(237, 237, 237).getRGB());

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

        heightOffset = 0;

        for (final Module m : Client.INSTANCE.getMM().getModules().values()) {
            if (m.getCategory() == selectedCat) {
                renderModule(x + categoryWidth + 5, y + categoryHeight + 5 + heightOffset, width - categoryWidth - 10, 20, m);
                heightOffset += 20;
            }
        }

    }
    public static Color changeHue(Color c, final float hue) {

        // Get saturation and brightness.
        final float[] hsbVals = new float[3];
        Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), hsbVals);

        // Pass .5 (= 180 degrees) as HUE
        c = new Color(Color.HSBtoRGB(hue, hsbVals[1], hsbVals[2]));

        return c;
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

        /*
        if (m.clickGuiOpacity != 1)
            RenderUtil.roundedRect(x, y, width, height, 5, new Color(39, 42, 48, Math.round(m.clickGuiOpacity)));

         */

        //Module description
        //if (m.descOpacityInGui > 1)
        CustomFont.drawStringSmall(m.getDescription(), x + (CustomFont.getWidth(m.getName())) + 6, y + 8, new Color(175, 175, 175, new Color(255, 255, 255).getAlpha()).getRGB());
    }

    public void renderModule(final float x, final float y, final float width, final float height, final String n) {
        //Module background
        RenderUtil.roundedRect(x, y, width, height, 5, new Color(255, 255, 255, 10));

        //Module name
        CustomFont.drawString(n, x + 4, y + 6, booleanColor2.hashCode());
    }

    public void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException {
        if (mouseButton == 0) {
            int amount = 0;
            for (final Category c : Category.values()) {
                if (mouseOver(x, y + categoryHeight * (amount + 1), categoryWidth, categoryHeight, mouseX, mouseY)) {
                    selectedCat = c;
                }
                ++amount;
            }
        }
    }




    protected void mouseReleased(final int mouseX, final int mouseY, final int state) {
        //if (blockScriptEditorOpen) GuiBlockScript.releasedMouseButton();
        super.mouseReleased(mouseX, mouseY, state);
    }

    public boolean mouseOver(final float posX, final float posY, final float width, final float height, final float mouseX, final float mouseY) {
        return mouseX > posX && mouseX < posX + width && mouseY > posY && mouseY < posY + height;
    }

    public void onGuiClosed() {
        Client.INSTANCE.getMM().getModule(ClickGUIModule.class).toggle();
    }
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
