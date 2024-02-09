package xyz.necrozma.notification;


import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import org.apache.commons.lang3.StringUtils;
import xyz.necrozma.gui.font.CustomFont;
import xyz.necrozma.gui.render.RenderUtil;
import xyz.necrozma.util.TimeUtil;


import java.awt.*;

public final class Notification {

    @Getter
    private final String description;
    private final NotificationType type;
    @Setter
    @Getter
    private long delay, start, end;

    private final ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());

    private float xVisual = sr.getScaledWidth();
    public float yVisual = sr.getScaledHeight() - 50;
    public float y = sr.getScaledHeight() - 50;

    private final TimeUtil timer = new TimeUtil();

    public Notification(final String description, final long delay, final NotificationType type) {
        this.description = description;
        this.delay = delay;
        this.type = type;

        start = System.currentTimeMillis();
        end = start + delay;
    }

    public void render() {
        final String name = StringUtils.capitalize(type.name().toLowerCase());

        final float screenWidth = sr.getScaledWidth();
        float x = (screenWidth) - (Math.max(CustomFont.getWidth(description), CustomFont.getWidth(name))) - 2;

        final float curr = System.currentTimeMillis() - getStart();
        final float percentageLeft = curr / getDelay();

        if (percentageLeft > 0.9) x = screenWidth;

        if (timer.hasReached(1000 / 60)) {
            xVisual = lerp(xVisual, x, 0.2f);
            yVisual = lerp(yVisual, y, 0.2f);

            timer.reset();
        }

        // final Color c = ThemeUtil.getThemeColor(ThemeType.GENERAL);

        // background color

        final Color c = new Color(159, 24, 242);

        RenderUtil.roundedRectCustom(xVisual, yVisual - 3, sr.getScaledWidth() - xVisual, 25, 4f, new Color(0, 0, 0, 170), true, false, true, false);

        Gui.drawRect((int) (xVisual + (percentageLeft * (CustomFont.getWidth(description)) + 8)), (int) (yVisual + 20), (int) (screenWidth + 1), (int) (yVisual + 22), c.hashCode());

        final Color bright = new Color(Math.min(c.getRed() + 16, 255), Math.min(c.getGreen() + 35, 255), Math.min(c.getBlue() + 7, 255));

        CustomFont.drawStringBold(name, xVisual + 1, yVisual - 2, bright.getRGB());
        CustomFont.drawString(description, xVisual + 1, yVisual + 10, c.hashCode());
    }

    public final float lerp(final float a, final float b, final float c) {
        return a + c * (b - a);
    }
}
