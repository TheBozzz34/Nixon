package xyz.necrozma.module.impl.motion;


import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.AxisAlignedBB;
import xyz.necrozma.event.impl.motion.BoundingBoxEvent;
import xyz.necrozma.event.impl.motion.PreMotionEvent;
import xyz.necrozma.module.Category;
import xyz.necrozma.module.Module;
import xyz.necrozma.module.ModuleInfo;
import xyz.necrozma.settings.impl.ModeSetting;
import xyz.necrozma.settings.impl.NumberSetting;

@ModuleInfo(name = "FastLadder", description = "Climbs up ladders faster than normal", category = Category.MOVEMENT)
public class FastLadder extends Module {

    private final ModeSetting mode = new ModeSetting("Mode", "Motion", "Motion", "Timer", "Position");
    private final NumberSetting speed = new NumberSetting("Speed", this, 1.5, 5, 0.1, 0.01);

    public FastLadder() {
    }

    @Override
    public void onPreMotion(final PreMotionEvent event) {
        // this.setSuffix(mode.getMode());
        if (mc.thePlayer.isOnLadder()) {
            switch (mode.getMode()) {
                case "Timer":
                    mc.timer.timerSpeed = (float) speed.getValue();
                    break;
                case "Motion":
                    mc.thePlayer.motionY = speed.getValue();
                    break;
                case "Position":
                    mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(
                            mc.thePlayer.posX, mc.thePlayer.posY + speed.getValue(), mc.thePlayer.posZ,
                            mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, false));
                    mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + speed.getValue(), mc.thePlayer.posZ);
                    break;
            }
        } else {
            mc.timer.timerSpeed = 1;
        }
    }

    @Override
    public void onDisable() {
        mc.timer.timerSpeed = 1;
        super.onDisable();
    }

}