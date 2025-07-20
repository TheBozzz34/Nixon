package xyz.necrozma.module.impl.render;




import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import xyz.necrozma.event.impl.render.Render3DEvent;
import xyz.necrozma.gui.render.RenderUtil;
import xyz.necrozma.module.Module;
import xyz.necrozma.module.Category;
import xyz.necrozma.module.ModuleInfo;
import xyz.necrozma.settings.impl.BooleanSetting;
import xyz.necrozma.settings.impl.ColorSetting;
import xyz.necrozma.settings.impl.ModeSetting;
import xyz.necrozma.util.ColorUtil;
import xyz.necrozma.util.MathUtil;
import xyz.necrozma.util.Pair;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

@ModuleInfo(name = "ChinaHat", description = "epic hat", category = Category.RENDER)
public class ChinaHat extends Module {

    private boolean rainbow = false;

    private final BooleanSetting firstPerson = new BooleanSetting("Show in first person", this, false);
    private final BooleanSetting allPlayers = new BooleanSetting("All players", this, false);
    //private final ModeSetting colorMode = new ModeSetting("Color Mode", "Sync", "Sync", "Custom");
    private final ColorSetting color = new ColorSetting("Color", Color.WHITE);

    public ChinaHat() {
    }

    @Override
    public void onRender3DEvent(final Render3DEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;


        float partialTicks = event.getPartialTicks();

        double renderPosX = mc.getRenderManager().renderPosX, renderPosY = mc.getRenderManager().renderPosY, renderPosZ = mc.getRenderManager().renderPosZ;

        glShadeModel(GL_SMOOTH);
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.disableCull();
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GlStateManager.disableTexture2D();
        GlStateManager.color(1, 1, 1, 1);
        OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        for (EntityPlayer player : mc.theWorld.playerEntities) {
            boolean self = player == mc.thePlayer;
            if ((!allPlayers.isEnabled() && !self) || (self && !firstPerson.isEnabled() && mc.gameSettings.thirdPersonView == 0)
                    || player.isDead || player.isInvisible() || (!self &&  !mc.thePlayer.canEntityBeSeen(player)))
                continue;

            glPushMatrix();

            double posX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks - renderPosX,
                    posY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks - renderPosY,
                    posZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks - renderPosZ;

            AxisAlignedBB bb = player.getEntityBoundingBox();

            //boolean lowerHeight = CustomModel.enabled && mc.gameSettings.thirdPersonView != 0;
            //double height = (lowerHeight ? -CustomModel.getYOffset() : 0) + bb.maxY - bb.minY + 0.02, radius = bb.maxX - bb.minX;
            boolean lowerHeight = false;
            double height = (lowerHeight ? -0.2 : 0) + bb.maxY - bb.minY + 0.02, radius = bb.maxX - bb.minX;

            float yaw = MathUtil.interpolate(player.prevRotationYawHead, player.rotationYawHead, partialTicks).floatValue();
            /*
            if (CLIENT.INSTANCE.isEnabled(KillAura.class) || Tenacity.INSTANCE.isEnabled(Scaffold.class)) {
                yaw = MathUtils.interpolate(MathHelper.wrapAngleTo180_float(player.prevRotationYawHead),
                        MathHelper.wrapAngleTo180_float(player.rotationYawHead), partialTicks).floatValue();
            }
             */


            float pitch = MathUtil.interpolate(player.prevRotationPitch, player.rotationPitch, partialTicks).floatValue();

            glTranslated(0, posY + height, 0);

            glEnable(GL_LINE_SMOOTH);
            glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
            if (self) glRotated(yaw, 0, -1, 0);
            glRotated(pitch / 3.0, 1, 0, 0);
            glTranslated(0, 0, pitch / 270.0);
            glLineWidth(2);
            glBegin(GL_LINE_LOOP);

            // outline/border or whatever you call it
            for (int i = 0; i <= 180; i++) {
                int color1 = getColor(i * 4, .5f).getRGB();
                Color color = new Color(color1);
                RenderUtil.color(color);
                glVertex3d(
                        posX - Math.sin(i * MathHelper.PI2 / 90) * radius,
                        -(player.isSneaking() ? 0.2 : 0) - 0.002,
                        posZ + Math.cos(i * MathHelper.PI2 / 90) * radius
                );
            }
            glEnd();

            glBegin(GL_TRIANGLE_FAN);
            int color12 = getColor(4, .7f).getRGB();
            Color color = new Color(color12);
            RenderUtil.color(color);
            glVertex3d(posX, 0.3 - (player.isSneaking() ? 0.23 : 0), posZ);

            // draw hat
            for (int i = 0; i <= 180; i++) {
                int color1 = getColor(i * 4, .2f).getRGB();
                Color color23 = new Color(color1);
                RenderUtil.color(color23);
                glVertex3d(posX - Math.sin(i * MathHelper.PI2 / 90) * radius,
                        -(player.isSneaking() ? 0.23F : 0),
                        posZ + Math.cos(i * MathHelper.PI2 / 90) * radius
                );

            }
            glVertex3d(posX, 0.3 - (player.isSneaking() ? 0.23 : 0), posZ);
            glEnd();
            glPopMatrix();
        }

        RenderUtil.resetColor();
        GlStateManager.enableCull();
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        glDisable(GL_LINE_SMOOTH);
        glShadeModel(GL_FLAT);
    }

    private Color getColor(int index, float alpha) {
        Color returnColor;
        returnColor = ColorUtil.rainbow(7, index, 1, 1, 1);
        return ColorUtil.applyOpacity(returnColor, alpha);
    }

}