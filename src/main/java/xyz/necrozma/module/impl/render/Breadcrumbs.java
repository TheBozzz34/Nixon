package xyz.necrozma.module.impl.render;


import net.minecraft.util.Vec3;
import org.lwjgl.input.Keyboard;
import xyz.necrozma.Client;
import xyz.necrozma.event.impl.motion.PreMotionEvent;
import xyz.necrozma.event.impl.render.Render3DEvent;
import xyz.necrozma.gui.render.RenderUtil;
import xyz.necrozma.module.Category;
import xyz.necrozma.module.Module;
import xyz.necrozma.module.ModuleInfo;
import xyz.necrozma.notification.NotificationType;
import xyz.necrozma.settings.impl.BooleanSetting;
import xyz.necrozma.settings.impl.NumberSetting;

import java.util.ArrayList;
import java.util.List;

@ModuleInfo(name = "Breadcrumbs", description = "Shows a trail where you walk", category = Category.RENDER)
public final class Breadcrumbs extends Module {

    public Breadcrumbs() {
        setKey(Keyboard.KEY_B);
    }

    List<Vec3> path = new ArrayList<>();

    private final BooleanSetting timeoutBool = new BooleanSetting("Timeout", this, true);
    private final NumberSetting timeout = new NumberSetting("Time", this, 15, 1, 150, 0.1);


    @Override
    public void onEnable() {
        path.clear();
    }

    @Override
    public void onPreMotion(final PreMotionEvent event) {

        if (mc.thePlayer.lastTickPosX != mc.thePlayer.posX || mc.thePlayer.lastTickPosY != mc.thePlayer.posY || mc.thePlayer.lastTickPosZ != mc.thePlayer.posZ) {
            path.add(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ));
        }

        if (timeoutBool.isEnabled())
            while (path.size() > (int) timeout.getValue()) {
                path.remove(0);
            }
    }

    @Override
    public void onRender3DEvent(final Render3DEvent event) {
        RenderUtil.renderBreadCrumbs(path);
    }
}
