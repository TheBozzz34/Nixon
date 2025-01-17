package xyz.necrozma.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MouseHelper;
import net.minecraft.util.ResourceLocation;
import net.optifine.CustomPanorama;
import net.optifine.CustomPanoramaProperties;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;
import xyz.necrozma.Client;
import xyz.necrozma.gui.font.CustomFont;
import xyz.necrozma.gui.font.TTFFontRenderer;
import xyz.necrozma.gui.render.RenderUtil;

import java.awt.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

public final class MainMenu extends GuiScreen {

    //Timer used to rotate the panorama, increases every tick.
    public static int panoramaTimer = 1500;

    //Path to images
    private static final ResourceLocation[] titlePanoramaPaths = new ResourceLocation[]{new ResourceLocation("nixon/panorama/panorama_0.png"), new ResourceLocation("rise/panorama/panorama_1.png"), new ResourceLocation("rise/panorama/panorama_2.png"), new ResourceLocation("rise/panorama/panorama_3.png"), new ResourceLocation("rise/panorama/panorama_4.png"), new ResourceLocation("rise/panorama/panorama_5.png")};

    // Font renderer
    private static final TTFFontRenderer fontRenderer = CustomFont.FONT_MANAGER.getFont("Dreamscape 96");

    //Positions
    private ScaledResolution sr;

    private float x;
    private float y;

    private int cocks;
    private static boolean rolled;

    private float screenWidth;
    private float screenHeight;

    private float buttonWidth = 50;
    private float buttonHeight = 20;
    private float gap = 4;
    public static float smoothedX, smoothedY;
    public static float xOffSet;
    public static float yOffSet;

    private boolean easterEgg;

    public float pitch;


    //Called from the main game loop to update the screen.
    public void updateScreen() {
        ++panoramaTimer;
    }

    public void initGui() {
        System.out.println("Opened Main Menu");
        panoramaTimer = 150;
        easterEgg = Math.random() > 0.99;
    }

    @Override
    public void onGuiClosed() {
        mc.timer.timerSpeed = 1;
    }

    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {

        if (mc.mouseHelper != null) mc.mouseHelper.mouseGrab(false);

        mc.timer.timerSpeed = 3f;


        RenderUtil.color(new Color(159, 24, 242));
        mc.getTextureManager().bindTexture(new ResourceLocation("nixon/bg/bg.png"));

        final float scale = 1.66f;
        final float amount = height;

        if (panoramaTimer % 100 == 0) {
            xOffSet = (float) (Math.random() - 0.5f) * amount;
            yOffSet = (float) (Math.random() - 0.5f) * amount;
        }

        smoothedX = (smoothedX * 250 + xOffSet) / 259;
        smoothedY = (smoothedY * 250 + yOffSet) / 259;

        drawModalRectWithCustomSizedTexture(0, 0, width / scale + smoothedX - 150, height / scale + smoothedY - 100, width, height, width * scale, height * scale);

        // Render the rise text
        screenWidth = fontRenderer.getWidth(Client.INSTANCE.getName());
        screenHeight = fontRenderer.getHeight(Client.INSTANCE.getName());

        final ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());

        UIUtil.logoPosition = /*MathUtil.lerp(UIUtil.logoPosition, */sr.getScaledHeight() / 2.0F - (screenHeight / 2.0F) - 6/*, 0.2f)*/;

        x = (sr.getScaledWidth() / 2.0F) - (screenWidth / 2.0F);
        y = (sr.getScaledHeight() / 2.0F) - (screenHeight / 2.0F) - 6;

        // Box
        RenderUtil.roundedRect(x - 10, y + fontRenderer.getHeight() + buttonHeight * 2 + gap * 2 + 2 - 108, 170, 145, 10, new Color(0, 0, 0, 35));

        if (easterEgg) {
            fontRenderer.drawString("RICE", x, UIUtil.logoPosition, new Color(255, 255, 255, 150).getRGB());
        } else {
            fontRenderer.drawString(Client.INSTANCE.getName(), x, UIUtil.logoPosition, new Color(255, 255, 255, 150).getRGB());
        }

        buttonWidth = 70;
        buttonHeight = 20;
        gap = 4;

        final ArrayList<String> changes = getStrings();

        if (sr.getScaledWidth() > 600 && sr.getScaledHeight() > 300) {
            CustomFont.drawString("Changelog:", 5, 5, new Color(255, 255, 255, 220).hashCode());

            for (int i = 0; i < changes.size(); i++) {
                CustomFont.drawString(changes.get(i), 5, 16 + i * 12, new Color(255, 255, 255, 220).hashCode());
            }
        }

        // Adjusting button positions
        float buttonY = y + fontRenderer.getHeight();
        float buttonYOffset = buttonHeight + gap + 2;

        // Singleplayer Button
        RenderUtil.roundedRect(x, y + fontRenderer.getHeight(), buttonWidth, buttonHeight + 2, 10, new Color(255, 255, 255, 35));
        CustomFont.drawString("Singleplayer", x + buttonWidth - 58, y + fontRenderer.getHeight() + 6, new Color(255, 255, 255, 240).hashCode());

        // Multiplayer Button
        RenderUtil.roundedRect(x + buttonWidth + gap, y + fontRenderer.getHeight(), buttonWidth, buttonHeight + 2, 10, new Color(255, 255, 255, 35));
        CustomFont.drawString("Multiplayer", x + buttonWidth * 2 + gap - 57, y + fontRenderer.getHeight() + 6, new Color(255, 255, 255, 240).hashCode());

        // Settings Button
        RenderUtil.roundedRect(x + buttonWidth + gap, y + fontRenderer.getHeight() + 2 + buttonHeight + gap, buttonWidth, buttonHeight + 2, 10, new Color(255, 255, 255, 35));
        CustomFont.drawString("Settings", x + buttonWidth * 2 + gap - 56, y + fontRenderer.getHeight() + buttonHeight + 10, new Color(255, 255, 255, 240).hashCode());

        // Quit Button
        RenderUtil.roundedRect(x, y + fontRenderer.getHeight() + 2 + buttonHeight + gap, buttonWidth, buttonHeight + 2, 10, new Color(255, 255, 255, 35));
        CustomFont.drawString("Quit", x + gap + 10, y + fontRenderer.getHeight() + buttonHeight + 10, new Color(255, 255, 255, 240).hashCode());

        // Login Button (Positioned below Quit)
        float loginButtonY = y + fontRenderer.getHeight() + 4 + buttonHeight * 2 + gap * 2;
        RenderUtil.roundedRect(x, loginButtonY, buttonWidth, buttonHeight + 2, 10, new Color(255, 255, 255, 35));
        CustomFont.drawString("Login", x + gap + 10, loginButtonY + 6, new Color(255, 255, 255, 240).hashCode());

        // Hover effects
        if (mouseOver(x, y + fontRenderer.getHeight(), buttonWidth, buttonHeight + 2, mouseX, mouseY)) {
            RenderUtil.roundedRect(x, y + fontRenderer.getHeight(), buttonWidth, buttonHeight + 2, 10, new Color(255, 255, 255, 55));
        }

        if (mouseOver(x + buttonWidth + gap, y + fontRenderer.getHeight(), buttonWidth, buttonHeight + 2, mouseX, mouseY)) {
            RenderUtil.roundedRect(x + buttonWidth + gap, y + fontRenderer.getHeight(), buttonWidth, buttonHeight + 2, 10, new Color(255, 255, 255, 55));
        }

        if (mouseOver(x + buttonWidth + gap, y + fontRenderer.getHeight() + 2 + buttonHeight + gap, buttonWidth, buttonHeight + 2, mouseX, mouseY)) {
            RenderUtil.roundedRect(x + buttonWidth + gap, y + fontRenderer.getHeight() + 2 + buttonHeight + gap, buttonWidth, buttonHeight + 2, 10, new Color(255, 255, 255, 55));
        }

        if (mouseOver(x, y + fontRenderer.getHeight() + 2 + buttonHeight + gap, buttonWidth, buttonHeight + 2, mouseX, mouseY)) {
            RenderUtil.roundedRect(x, y + fontRenderer.getHeight() + 2 + buttonHeight + gap, buttonWidth, buttonHeight + 2, 10, new Color(255, 255, 255, 55));
        }

        if (mouseOver(x, loginButtonY, buttonWidth, buttonHeight + 2, mouseX, mouseY)) {
            RenderUtil.roundedRect(x, loginButtonY, buttonWidth, buttonHeight + 2, 10, new Color(255, 255, 255, 55));
        }

        // Version and message rendering
        String version = "Nixon " + Client.INSTANCE.getVersion();
        CustomFont.drawString(version, 2, sr.getScaledHeight() - 12.5, new Color(255, 255, 255, 180).hashCode());

        final String message = "Made with <3 by Necrozma";
        CustomFont.drawString(message, sr.getScaledWidth() - CustomFont.getWidth(message) - 2, sr.getScaledHeight() - 12.5, new Color(255, 255, 255, 180).hashCode());

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @NotNull
    private static ArrayList<String> getStrings() {
        final ArrayList<String> changes = new ArrayList<>();

        changes.add("");
        changes.add("d6f9055");
        changes.add("+ Added Gaussian Blur internal function");
        changes.add("+ Added new internal framebuffer functions");
        changes.add("+ Modules now have internal flag to be hidden in gui");
        changes.add("+ Added utils to read/write to files");
        changes.add("+ Added more OpenGL utils");
        changes.add("+ Added more math functions");
        changes.add("+ Added roundedUtil and stencilUti ");

        changes.add("");
        changes.add("8cbd1bc");
        changes.add("+ ClickGui no longer pauses game when opened");
        changes.add("+ Added blur to inventory background");
        changes.add("+ Added blur to pause menu background");
        changes.add("+ Removed notifications when a module is toggled");
        return changes;
    }

    public void mouseClicked(final int mouseX, final int mouseY, final int button) {
        // Singleplayer Button
        if (mouseOver(x, y + fontRenderer.getHeight(), buttonWidth, buttonHeight + 2, mouseX, mouseY)) {
            mc.displayGuiScreen(new GuiSelectWorld(this));
        }

        // Multiplayer Button
        if (mouseOver(x + buttonWidth + gap, y + fontRenderer.getHeight(), buttonWidth, buttonHeight + 2, mouseX, mouseY)) {
            mc.displayGuiScreen(new GuiMultiplayer(this));
        }

        // Settings Button
        if (mouseOver(x + buttonWidth + gap, y + fontRenderer.getHeight() + 2 + buttonHeight + gap, buttonWidth, buttonHeight + 2, mouseX, mouseY)) {
            mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings));
        }

        // Quit Button
        if (mouseOver(x, y + fontRenderer.getHeight() + 2 + buttonHeight + gap, buttonWidth, buttonHeight + 2, mouseX, mouseY)) {
            System.exit(-1);
        }

        // Login Button
        float loginButtonY = y + fontRenderer.getHeight() + 4 + buttonHeight * 2 + gap * 2;
        if (mouseOver(x, loginButtonY, buttonWidth, buttonHeight + 2, mouseX, mouseY)) {
            mc.displayGuiScreen(new LoginGui());
        }
    }

    public boolean mouseOver(final float posX, final float posY, final float width, final float height, final float mouseX, final float mouseY) {
        if (mouseX > posX && mouseX < posX + width) {
            return mouseY > posY && mouseY < posY + height;
        }
        return false;
    }

    private void drawPanorama(final int p_73970_1_, final int p_73970_2_, final float p_73970_3_) {
        final Tessellator tessellator = Tessellator.getInstance();
        final WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        GlStateManager.matrixMode(5889);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        Project.gluPerspective(120.0F, 1.0F, 0.05F, 10.0F);
        GlStateManager.matrixMode(5888);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.disableCull();
        GlStateManager.depthMask(false);
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        final int i = 8;
        int j = 64;

        final CustomPanoramaProperties custompanoramaproperties = CustomPanorama.getCustomPanoramaProperties();

        if (custompanoramaproperties != null) {
            j = custompanoramaproperties.getBlur1();
        }

        for (int k = 0; k < j; ++k) {
            GlStateManager.pushMatrix();

            final float f = ((float) (k % i) / (float) i - 0.5F) / 64.0F;
            final float f1 = ((float) (k / i) / (float) i - 0.5F) / 64.0F;
            final float f2 = 0.0F;

            GlStateManager.translate(f, f1, f2);
            GlStateManager.rotate(MathHelper.sin(((float) panoramaTimer + p_73970_3_) / 400.0F) * 25.0F + 20.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(-((float) panoramaTimer + p_73970_3_) * 0.1F, 0.0F, 1.0F, 0.0F);

            for (int l = 0; l < 6; ++l) {
                GlStateManager.pushMatrix();

                if (l == 1) {
                    GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
                }

                if (l == 2) {
                    GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                }

                if (l == 3) {
                    GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
                }

                if (l == 4) {
                    GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
                }

                if (l == 5) {
                    GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
                }

                ResourceLocation[] aresourcelocation = titlePanoramaPaths;

                if (custompanoramaproperties != null) {
                    aresourcelocation = custompanoramaproperties.getPanoramaLocations();
                }

                this.mc.getTextureManager().bindTexture(aresourcelocation[l]);
                worldrenderer.begin(7, DefaultVertexFormats.field_181709_i);
                final int i1 = 255 / (k + 1);
                final float f3 = 0.0F;
                worldrenderer.pos(-1.0D, -1.0D, 1.0D).func_181673_a(0.0D, 0.0D).color(255, 255, 255, i1).endVertex();
                worldrenderer.pos(1.0D, -1.0D, 1.0D).func_181673_a(1.0D, 0.0D).color(255, 255, 255, i1).endVertex();
                worldrenderer.pos(1.0D, 1.0D, 1.0D).func_181673_a(1.0D, 1.0D).color(255, 255, 255, i1).endVertex();
                worldrenderer.pos(-1.0D, 1.0D, 1.0D).func_181673_a(0.0D, 1.0D).color(255, 255, 255, i1).endVertex();
                tessellator.draw();
                GlStateManager.popMatrix();
            }

            GlStateManager.popMatrix();
            GlStateManager.colorMask(true, true, true, false);
        }

        worldrenderer.setTranslation(0.0D, 0.0D, 0.0D);
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.matrixMode(5889);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(5888);
        GlStateManager.popMatrix();
        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.enableDepth();
    }

    private void renderSkybox(final int p_73971_1_, final int p_73971_2_, final float p_73971_3_) {
        GlStateManager.disableAlpha();
        this.mc.getFramebuffer().unbindFramebuffer();
        GlStateManager.viewport(0, 0, 256, 256);
        this.drawPanorama(p_73971_1_, p_73971_2_, p_73971_3_);
        this.rotateAndBlurSkybox(p_73971_3_);
        int i = 3;
        final CustomPanoramaProperties custompanoramaproperties = CustomPanorama.getCustomPanoramaProperties();

        if (custompanoramaproperties != null) {
            i = custompanoramaproperties.getBlur3();
        }

        for (int j = 0; j < i; ++j) {
            this.rotateAndBlurSkybox(p_73971_3_);
            this.rotateAndBlurSkybox(p_73971_3_);
        }

        this.mc.getFramebuffer().bindFramebuffer(true);
        GlStateManager.viewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);
        final float f2 = this.width > this.height ? 120.0F / (float) this.width : 120.0F / (float) this.height;
        final float f = (float) this.height * f2 / 256.0F;
        final float f1 = (float) this.width * f2 / 256.0F;
        final int k = this.width;
        final int l = this.height;
        final Tessellator tessellator = Tessellator.getInstance();
        final WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.field_181709_i);
        worldrenderer.pos(0.0D, l, zLevel).func_181673_a(0.5F - f, 0.5F + f1).func_181666_a(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        worldrenderer.pos(k, l, zLevel).func_181673_a(0.5F - f, 0.5F - f1).func_181666_a(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        worldrenderer.pos(k, 0.0D, zLevel).func_181673_a(0.5F + f, 0.5F - f1).func_181666_a(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        worldrenderer.pos(0.0D, 0.0D, zLevel).func_181673_a(0.5F + f, 0.5F + f1).func_181666_a(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        tessellator.draw();
        GlStateManager.enableAlpha();
    }

    private void rotateAndBlurSkybox(final float p_73968_1_) {
        this.mc.getTextureManager().bindTexture(new ResourceLocation("nixon/bg/bg.png"));
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0, 256, 256);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.colorMask(true, true, true, false);
        final Tessellator tessellator = Tessellator.getInstance();
        final WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.field_181709_i);
        GlStateManager.disableAlpha();
        final int i = 3;
        final int j = 3;
        final CustomPanoramaProperties custompanoramaproperties = CustomPanorama.getCustomPanoramaProperties();

        for (int k = 0; k < j; ++k) {
            final float f = 1.0F / (float) (k + 1);
            final int l = this.width;
            final int i1 = this.height;
            final float f1 = (float) (k - i / 2) / 256.0F;
            worldrenderer.pos(l, i1, zLevel).func_181673_a(0.0F + f1, 1.0D).func_181666_a(1.0F, 1.0F, 1.0F, f).endVertex();
            worldrenderer.pos(l, 0.0D, zLevel).func_181673_a(1.0F + f1, 1.0D).func_181666_a(1.0F, 1.0F, 1.0F, f).endVertex();
            worldrenderer.pos(0.0D, 0.0D, zLevel).func_181673_a(1.0F + f1, 0.0D).func_181666_a(1.0F, 1.0F, 1.0F, f).endVertex();
            worldrenderer.pos(0.0D, i1, zLevel).func_181673_a(0.0F + f1, 0.0D).func_181666_a(1.0F, 1.0F, 1.0F, f).endVertex();
        }

        tessellator.draw();
        GlStateManager.enableAlpha();
        GlStateManager.colorMask(true, true, true, true);
    }


    public static boolean openWebpage(final URI uri) {
        final Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(uri);
                return true;
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

}
