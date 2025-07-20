package xyz.necrozma.gui.strikeless;


import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import org.lwjgl.input.Mouse;
import xyz.necrozma.Client;
import xyz.necrozma.gui.ClickGuiNG.ClickGUIType;
import xyz.necrozma.module.Category;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StrikeGUI extends GuiScreen implements ClickGUIType {

    public static float scrollHorizontal;
    private final List<ClickFrame> frames = new ArrayList<>();
    private float lastScrollHorizontal;

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    public void updateScroll() {
        if (GuiInventory.isCtrlKeyDown()) {
            final float partialTicks = mc == null || mc.timer == null ? 1.0F : mc.timer.renderPartialTicks;

            final float lastLastScrollHorizontal = lastScrollHorizontal;
            lastScrollHorizontal = scrollHorizontal;
            final float wheel = Mouse.getDWheel();
            scrollHorizontal += wheel / 10.0F;
            if (wheel == 0) scrollHorizontal -= (lastLastScrollHorizontal - scrollHorizontal) * 0.6 * partialTicks;
        }
    }

    @Override
    public void initGui() {
        super.initGui();

        if (frames.size() <= 0) {
            int index = -1;
            for (final Category category : Category.values()) {
                if (category == Category.STATISTICS)
                    continue;

                final ClickFrame frame = new ClickFrame(category, 20 + (++index * (ClickFrame.entryWidth + 20)), 20);
                if (category == Category.CONFIGS) frame.setExpanded(false);
                frames.add(frame);
            }
        }
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        frames.forEach(frame -> frame.draw(this, mouseX, mouseY));
        frames.forEach(frame -> frame.drawDescriptions(mouseX, mouseY, partialTicks));
    }

    @Override
    public void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException {
        frames.forEach(frame -> frame.mouseClicked(mouseX, mouseY, mouseButton));
    }

    @Override
    public void mouseReleased(final int mouseX, final int mouseY, final int mouseButton) {
        frames.forEach(frame -> frame.mouseReleased(mouseX, mouseY, mouseButton));
    }

    @Override
    public void mouseClickMove(final int mouseX, final int mouseY, final int clickedMouseButton, final long timeSinceLastClick) {
        frames.forEach(frame -> frame.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick));
    }

    @Override
    public void onGuiClosed() {
        Objects.requireNonNull(Client.INSTANCE.getMM().getModuleFromString("ClickGui")).toggle();
    }
}