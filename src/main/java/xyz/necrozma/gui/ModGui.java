package xyz.necrozma.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import xyz.necrozma.Client;
import xyz.necrozma.module.Module;

import java.util.concurrent.atomic.AtomicInteger;

public class ModGui extends GuiScreen {

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        ScaledResolution scaledResolution = new ScaledResolution(mc);

        int centerX = scaledResolution.getScaledWidth() / 2;
        int centerY = scaledResolution.getScaledHeight() / 2;

        int squareSize = 50;

        AtomicInteger yOffSet = new AtomicInteger(4);
        int yOffsetInc = 10;


        // drawRect(centerX - squareSize / 2, centerY - squareSize / 2, centerX + squareSize / 2, centerY + squareSize / 2, 0xFFFF0000);

        drawDefaultBackground();

        Client.INSTANCE.getMM().getModules().values().forEach(module -> {
            if (module.isToggled()) {
                drawBounds(module, 2, yOffSet.get());
                drawString(mc.fontRendererObj, module.getName(),2, yOffSet.get() + 2, 0xFF00FF00);
                yOffSet.addAndGet(yOffsetInc);
            } else {
                drawBounds(module, 2, yOffSet.get());
                drawString(mc.fontRendererObj, module.getName(),  2, yOffSet.get() + 2, 0xFFFF0000);
                yOffSet.addAndGet(yOffsetInc);
            }
        });

        // drawBouncingBox(); Just for fun


    }

    private void drawBounds(Module module, int x, int y) {
        int textWidth = mc.fontRendererObj.getStringWidth(module.getName());
        int width = textWidth + 2;
        int height = 12;
        drawRect(x, y, x + width, y + height, 0x90000000);
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
}