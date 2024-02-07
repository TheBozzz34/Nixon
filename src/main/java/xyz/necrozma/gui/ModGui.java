package xyz.necrozma.gui;

import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import xyz.necrozma.Client;
import xyz.necrozma.module.impl.misc.Nuker;
import xyz.necrozma.module.impl.render.Fullbright;
import xyz.necrozma.module.impl.render.Xray;
import xyz.necrozma.settings.Settings;
import xyz.necrozma.settings.impl.NumberSetting;

import java.util.concurrent.atomic.AtomicInteger;


public class ModGui extends GuiScreen {

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        ScaledResolution scaledResolution = new ScaledResolution(mc);

        int centerX = scaledResolution.getScaledWidth() / 2;
        int centerY = scaledResolution.getScaledHeight() / 2;

        int squareSize = 50;

        AtomicInteger yOffSet = new AtomicInteger(14);
        int yOffsetInc = 10;


        // drawRect(centerX - squareSize / 2, centerY - squareSize / 2, centerX + squareSize / 2, centerY + squareSize / 2, 0xFFFF0000);

        drawDefaultBackground();

        drawString(mc.fontRendererObj, "Xrayed blocks", 4, 2, 0xFFFFFFFF);

        drawHorizontalLine(0, scaledResolution.getScaledWidth(), 12, 0xFFFFFFFF);

        for (Block block : Xray.BLOCKS) {
            drawString(mc.fontRendererObj, block.getLocalizedName(), 4, yOffSet.get(), 0xFFFFFFFF);
            yOffSet.addAndGet(yOffsetInc);
        }

        // drawBouncingBox(); Just for fun

        drawString(mc.fontRendererObj, "Settings", scaledResolution.getScaledWidth() - mc.fontRendererObj.getStringWidth("Settings") - 2, 2, 0xFFFFFFFF);

        drawNukerInfo();

        drawFullbrightInfo();


    }



    private int x = 0;
    private int y = 0;
    private int xSpeed = 1;
    private int ySpeed = 1;
    private int squareSize = 50;
    private void drawBouncingBox() {
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        int screenWidth = scaledResolution.getScaledWidth();
        int screenHeight = scaledResolution.getScaledHeight();

        x += xSpeed;
        y += ySpeed;

        if (x <= 0 || x >= screenWidth - squareSize) {
            xSpeed *= -1;
        }

        if (y <= 0 || y >= screenHeight - squareSize) {
            ySpeed *= -1;
        }
        // Draw the rectangle at the updated position
        drawRect(x, y, x + squareSize, y + squareSize, 0xFFFF0000); // Change the color as needed
    }

    private void drawNukerInfo() {
        Settings Settings = Client.INSTANCE.getMM().getModule(Nuker.class).getSetting("Range");

        final double radius = ((NumberSetting) Settings).getValue();
        final int range = (int) radius * 2;

        int screenWidth = new ScaledResolution(mc).getScaledWidth();
        String text = "Nuker radius: " + radius + " (" + range + "x" + range + "x" + range + ")";
        int textWidth = mc.fontRendererObj.getStringWidth(text);
        int x = screenWidth - textWidth - 2;

        drawString(mc.fontRendererObj, text, x, 14, 0xFFFFFFFF);
    }

    private void drawFullbrightInfo() {
        Settings Settings = Client.INSTANCE.getMM().getModule(Fullbright.class).getSetting("Gamma");

        final double gamma = ((NumberSetting) Settings).getValue();

        int screenWidth = new ScaledResolution(mc).getScaledWidth();
        String text = "Fullbright gamma: " + gamma;
        int textWidth = mc.fontRendererObj.getStringWidth(text);
        int x = screenWidth - textWidth - 2;

        drawString(mc.fontRendererObj, text, x, 24, 0xFFFFFFFF);
    }
}