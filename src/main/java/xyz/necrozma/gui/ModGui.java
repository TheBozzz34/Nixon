package xyz.necrozma.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import xyz.necrozma.Client;
import xyz.necrozma.module.Module;

import java.io.IOException;
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
               drawString(mc.fontRendererObj, module.getName(), 2, yOffSet.get(), 0xFF00FF00);
            } else {
                drawString(mc.fontRendererObj, module.getName(), 2, yOffSet.get(), 0xFFFF0000);
            }
            yOffSet.addAndGet(yOffsetInc);
        });
    }
}