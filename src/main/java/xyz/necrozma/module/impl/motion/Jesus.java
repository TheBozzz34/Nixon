package xyz.necrozma.module.impl.motion;




import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.util.AxisAlignedBB;
import xyz.necrozma.event.impl.motion.BlockCollideEvent;
import xyz.necrozma.event.impl.motion.PreMotionEvent;
import xyz.necrozma.module.Category;
import xyz.necrozma.module.Module;
import xyz.necrozma.module.ModuleInfo;
import xyz.necrozma.settings.impl.ModeSetting;
import xyz.necrozma.settings.impl.NumberSetting;
import xyz.necrozma.util.MoveUtil;
import xyz.necrozma.util.PlayerUtil;

import java.util.Objects;

/**
 * Gives you a boost of speed or an improved strafing.
 * Who the fuck made these descriptions ^^^
 */
@ModuleInfo(name = "Jesus", description = "Allows you to walk on water like Jesus", category = Category.MOVEMENT)
public final class Jesus extends Module {

    private final ModeSetting mode = new ModeSetting("Mode", this, "Solid", "Solid", "Boost", "Jump", "NCP");
    private final NumberSetting motion = new NumberSetting("Motion", this, 0.2, 0.0, 5, 0.1);
    private final NumberSetting speed = new NumberSetting("Speed", this, 1, 0.1, 9.5, 0.1);

    @Override
    public void onUpdateAlwaysInGui() {
        motion.hidden = mode.is("Solid") || mode.is("NCP");

        speed.hidden = !mode.is("Boost");
    }

    @Override
    public void onPreMotion(final PreMotionEvent event) {
        if (mc.gameSettings.keyBindSneak.isKeyDown())
            return;

        final float ascensionValue = 0.06000000238418583F; // Fastest legit water ascension value

        switch (mode.getMode()) {
            case "Boost":
                if (mc.thePlayer.isInLiquid() || (mc.thePlayer.movementInput.jump && PlayerUtil.isOnLiquid()))
                    mc.thePlayer.motionY = ascensionValue;
                else if (PlayerUtil.isOnLiquid()) {
                    mc.thePlayer.motionY = motion.getValue();
                    MoveUtil.strafe(speed.getValue());
                    mc.thePlayer.onGround = true;
                }
                break;

            case "Jump":
                if (mc.thePlayer.isInLiquid() || (mc.thePlayer.movementInput.jump && PlayerUtil.isOnLiquid()))
                    mc.thePlayer.motionY = ascensionValue;
                else if (PlayerUtil.isOnLiquid()) {
                    mc.thePlayer.motionY = motion.getValue();
                    mc.thePlayer.onGround = true;
                }
                break;

            case "Solid":
                if (mc.thePlayer.isInLiquid() || (mc.thePlayer.movementInput.jump && PlayerUtil.isOnLiquid()))
                    mc.thePlayer.motionY = ascensionValue;
                break;

            case "NCP":
                if (mc.thePlayer.isInLiquid() || (mc.thePlayer.movementInput.jump && PlayerUtil.isOnLiquid()))
                    mc.thePlayer.motionY = ascensionValue;
                //else if (PlayerUtil.isOnLiquid() && !Objects.requireNonNull(Rise.INSTANCE.getModuleManager().getModule("Speed")).isEnabled() && mc.thePlayer.ticksExisted % 2 == 0)
                    //event.setY(event.getY() - 0.015625);
                break;
        }
    }

    /*
    @Override
    public void onBlockCollide(final BlockCollideEvent event) {
        if (mc.thePlayer.movementInput == null || mc.thePlayer.movementInput.sneak || mc.thePlayer.movementInput.jump)
            return;

        final Block block = event.getBlock();

        switch (mode.getMode()) {
            case "Solid":
                if (block instanceof BlockLiquid && !mc.thePlayer.isInLiquid())
                    event.setCollisionBoundingBox(AxisAlignedBB.fromBounds(event.getX(), event.getY(), event.getZ(), event.getX() + 1, event.getY() + 1, event.getZ() + 1));
                break;

            case "NCP":
                //if (block instanceof BlockLiquid && !Objects.requireNonNull(Rise.INSTANCE.getModuleManager().getModule("Speed")).isEnabled() && !mc.thePlayer.isInLiquid())
                    //event.setCollisionBoundingBox(AxisAlignedBB.fromBounds(event.getX(), event.getY(), event.getZ(), event.getX() + 1, event.getY() + 1, event.getZ() + 1));
                break;
        }
    }

     */
}
