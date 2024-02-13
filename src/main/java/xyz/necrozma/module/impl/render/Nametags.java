package xyz.necrozma.module.impl.render;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;
import xyz.necrozma.event.impl.render.Render3DEvent;
import xyz.necrozma.gui.font.CustomFont;
import xyz.necrozma.gui.font.TTFFontRenderer;
import xyz.necrozma.gui.render.RenderUtil;
import xyz.necrozma.module.Category;
import xyz.necrozma.module.Module;
import xyz.necrozma.module.ModuleInfo;

import java.awt.*;


@ModuleInfo(name = "Nametags", description = "Displays nametags", category = Category.RENDER)
public class Nametags extends Module {

    private final TTFFontRenderer comfortaa = CustomFont.FONT_MANAGER.getFont("Comfortaa 128");
    public Nametags() {
    }

    @Override
    public void onRender3DEvent(final Render3DEvent event) {
        int amount = 0;

        for (final EntityPlayer entity : mc.theWorld.playerEntities) {
            if (entity != null) {
                final String name = entity.getDisplayName().getUnformattedTextForChat();

                if ((!entity.isInvisible() && !entity.isDead && entity != mc.thePlayer && RenderUtil.isInViewFrustrum(entity))) {
                    //Changing size
                    final float scale = Math.max(0.02F, mc.thePlayer.getDistanceToEntityRender(entity) / 300);

                    final double x = (entity).lastTickPosX + ((entity).posX - (entity).lastTickPosX) * mc.timer.renderPartialTicks - (mc.getRenderManager()).renderPosX;
                    final double y = ((entity).lastTickPosY + ((entity).posY - (entity).lastTickPosY) * mc.timer.renderPartialTicks - (mc.getRenderManager()).renderPosY) + scale * 6;
                    final double z = (entity).lastTickPosZ + ((entity).posZ - (entity).lastTickPosZ) * mc.timer.renderPartialTicks - (mc.getRenderManager()).renderPosZ;

                    GL11.glPushMatrix();
                    GL11.glTranslated(x, y + 2.3, z);
                    GlStateManager.disableDepth();

                    GL11.glScalef(-scale, -scale, -scale);

                    GL11.glRotated(-mc.getRenderManager().playerViewY, 0.0D, 1.0D, 0.0D);
                    GL11.glRotated(mc.getRenderManager().playerViewX, mc.gameSettings.thirdPersonView == 2 ? -1.0D : 1.0D, 0.0D, 0.0D);

                    final float width = CustomFont.getWidthProtect(name) - 7;
                    final float progress = Math.min((entity).getHealth(), (entity).getMaxHealth()) / (entity).getMaxHealth();

                    //final Color color = ThemeUtil.getThemeColor(amount, ThemeType.GENERAL, 0.5f);
                    final Color color = new Color(159, 24, 242);

                    Gui.drawRect((int) (-width / 2.0F - 5.0F), -1, (int) (width / 2.0F + 5.0F), 8, 0x40000000);
                    Gui.drawRect((int) (-width / 2.0F - 5.0F), 7, (int) (-width / 2.0F - 5.0F + (width / 2.0F + 5.0F - -width / 2.0F + 5.0F) * progress), 8, color.getRGB());

                    GL11.glScalef(0.1f, 0.1f, 0.1f);

                    comfortaa.drawCenteredString(name, -width / 16.0F, 0.5f, -1);

                    GL11.glScalef(1.9f, 1.9f, 1.9f);

                    GlStateManager.enableDepth();
                    GL11.glPopMatrix();
                    amount++;
                }
            }
        }
    }
    @Override
    public void onEnable() {
        super.onEnable();
    }
    @Override
    public void onDisable() {
        super.onDisable();
    }
    @Override
    public void onToggle() {
        super.onToggle();
    }
}
