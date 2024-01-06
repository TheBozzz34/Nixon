package xyz.necrozma.module.impl.misc;

import lombok.Getter;
import me.zero.alpine.listener.Listener;
import me.zero.alpine.listener.Subscribe;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import org.lwjgl.input.Keyboard;
import xyz.necrozma.event.impl.update.EventUpdate;
import xyz.necrozma.module.Category;
import xyz.necrozma.module.Module;
import xyz.necrozma.module.ModuleInfo;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.server.S02PacketChat;
import xyz.necrozma.settings.impl.BooleanSetting;
import xyz.necrozma.settings.impl.NumberSetting;
import xyz.necrozma.util.ChatUtil;
import xyz.necrozma.util.PacketUtil;

@ModuleInfo(name = "Nuker", description = "Breaks blocks around you", category = Category.MISC)
public class Nuker extends Module {

    private final NumberSetting range = new NumberSetting("Range", this, 4, 1, 7, 0.5);

    private final BooleanSetting swing = new BooleanSetting("Swing", this, true);

    public Nuker() {
        setKey(Keyboard.KEY_N);
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

    @Subscribe
    private final Listener<EventUpdate> listener = new Listener<>(e -> {
        if ((mc.thePlayer.ticksExisted % 20 == 0)) {
            final double radius = this.range.getValue() - 1;
            nuke(radius, mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
        }
    });

    private void nuke(final double range, final double xPos, final double yPos, final double zPos) {
        if (range == 0) {
            final BlockPos blockPos = new BlockPos(xPos, yPos, zPos);
            final Block block = mc.theWorld.getBlockState(blockPos).getBlock();

            if (block instanceof BlockAir /* || (block.getBlockHardness(getMc().theWorld, blockPos) > 0 ) */)
                return;

            PacketUtil.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, blockPos, EnumFacing.UP));
            PacketUtil.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, blockPos, EnumFacing.UP));

            if (swing.isEnabled())
                mc.thePlayer.swingItem();
        } else {
            for (double x = -range; x < range; x++) {
                for (double y = range; y > -range; y--) {
                    for (double z = -range; z < range; z++) {
                        // if (scatter.isEnabled() && !((mc.thePlayer.ticksExisted % 2 == 0 ? x : z) % 2 == 0))
                          //  continue;

                        final BlockPos blockPos = new BlockPos(xPos + x, yPos + y, zPos + z);
                        final Block block = mc.theWorld.getBlockState(blockPos).getBlock();

                        if (block instanceof BlockAir /*|| (block.getBlockHardness(getMc().theWorld, blockPos) > 0 )*/)
                            continue;

                        PacketUtil.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, blockPos, EnumFacing.UP));
                        PacketUtil.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, blockPos, EnumFacing.UP));

                        if (swing.isEnabled())
                            mc.thePlayer.swingItem();
                    }
                }
            }
        }
    }


}
