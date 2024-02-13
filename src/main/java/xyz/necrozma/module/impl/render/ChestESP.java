package xyz.necrozma.module.impl.render;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityEnderChest;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import xyz.necrozma.event.impl.render.Render3DEvent;
import xyz.necrozma.gui.render.RenderUtil;
import xyz.necrozma.module.Category;
import xyz.necrozma.module.Module;
import xyz.necrozma.module.ModuleInfo;

import java.awt.*;

@ModuleInfo(name = "ChestESP", description = "Highlights chests", category = Category.RENDER)
public class ChestESP extends Module {

    private TileEntity[] tileEntities = new TileEntity[0];

    public ChestESP() {
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
    public void onRender3DEvent(final Render3DEvent event) {
        super.onRender3DEvent(event);

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glPushMatrix();

        int amount = 0;
        for (final TileEntity tileEntity : mc.theWorld.loadedTileEntityList) {
            if (tileEntity instanceof TileEntityChest || tileEntity instanceof TileEntityEnderChest) {
                render(amount, tileEntity);
                amount++;
            }
        }

        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }

    private void render(final int amount, final TileEntity p) {
        GL11.glPushMatrix();

        final RenderManager renderManager = mc.getRenderManager();

        final double x = (p.getPos().getX() + 0.5) - renderManager.renderPosX;
        final double y = p.getPos().getY() - renderManager.renderPosY;
        final double z = (p.getPos().getZ() + 0.5) - renderManager.renderPosZ;

        GL11.glTranslated(x, y, z);

        GL11.glRotated(-renderManager.playerViewY, 0.0D, 1.0D, 0.0D);
        GL11.glRotated(renderManager.playerViewX, mc.gameSettings.thirdPersonView == 2 ? -1.0D : 1.0D, 0.0D, 0.0D);

        final float scale = 1 / 100f;
        GL11.glScalef(-scale, -scale, scale);

        //final Color c = ThemeUtil.getThemeColor(amount, ThemeType.GENERAL, 0.5f);
        final Color c = new Color(159, 24, 242);

        final float offset = renderManager.playerViewX * 0.5f;

        RenderUtil.lineNoGl(-50, offset, 50, offset, c);
        RenderUtil.lineNoGl(-50, -95 + offset, -50, offset, c);
        RenderUtil.lineNoGl(-50, -95 + offset, 50, -95 + offset, c);
        RenderUtil.lineNoGl(50, -95 + offset, 50, offset, c);

        GL11.glPopMatrix();
    }

    private void addTileEntity(TileEntity tileEntity) {
        TileEntity[] tileEntities = this.tileEntities;
        int length = tileEntities.length;
        TileEntity[] newTileEntities = new TileEntity[length + 1];
        System.arraycopy(tileEntities, 0, newTileEntities, 0, length);
        newTileEntities[length] = tileEntity;
        this.tileEntities = newTileEntities;
    }
}
