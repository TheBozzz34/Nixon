package xyz.necrozma.gui.render;


import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;
import xyz.necrozma.gui.font.CustomFont;
import xyz.necrozma.gui.font.TTFFontRenderer;


import java.awt.*;

public class KeystrokeUtil {

    int ticksSinceLastPress;

    KeyBinding key;

    private final TTFFontRenderer comfortaa = CustomFont.FONT_MANAGER.getFont("Comfortaa 18");

    public void setUpKey(final KeyBinding k) {
        key = k;
    }

    public void drawButton(final double x, final double y, final double width) {
        final String keyName = Keyboard.getKeyName(key.getKeyCode());

        float offset = -5;

        switch (keyName) {
            case "A":
            case "D":
                offset = -4;
                break;
            case "S":
                offset = -3.5f;
                break;
        }

        if (key == Minecraft.getMinecraft().gameSettings.keyBindJump) {
            RenderUtil.roundedRect(x - width - 4, y, width * 3 + 8, width, width, new Color(0, 0, 0, 55 + ticksSinceLastPress));
            comfortaa.drawString(keyName, (float) (x), (float) (y + 15 - 2.5), -1);
        } else {
            RenderUtil.circle(x, y, width, new Color(0, 0, 0, 55 + ticksSinceLastPress));
            comfortaa.drawString(keyName, (float) (x + 15 + offset), (float) (y + 15 - 2.7), -1);
        }
    }

    public void updateAnimations() {

        if (key == null)
            return;

        if (key.isKeyDown())
            ticksSinceLastPress += 1;
        else
            ticksSinceLastPress -= 1;

        if (ticksSinceLastPress > 55)
            ticksSinceLastPress = 55;

        if (ticksSinceLastPress < 0)
            ticksSinceLastPress = 0;

    }

    public void blur(final double x, final double y, final double width) {

        if (key == null)
            return;

        final String keyName = Keyboard.getKeyName(key.getKeyCode());
        if (key == Minecraft.getMinecraft().gameSettings.keyBindJump) {
            comfortaa.drawString(keyName, (float) (x), (float) (y + 15 - 2.5), Color.BLACK.hashCode());
        } else {
            RenderUtil.circle(x, y, width, Color.BLACK);
        }

    }

}
