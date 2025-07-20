package xyz.necrozma.module.impl.combat;


import net.minecraft.network.play.client.*;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import xyz.necrozma.Client;
import xyz.necrozma.event.impl.motion.PreMotionEvent;
import xyz.necrozma.event.impl.packet.EventPacket;
import xyz.necrozma.module.Category;
import xyz.necrozma.module.Module;
import xyz.necrozma.module.ModuleInfo;
import xyz.necrozma.settings.impl.BooleanSetting;
import xyz.necrozma.settings.impl.NumberSetting;
import xyz.necrozma.util.PlayerUtil;

@ModuleInfo(name = "AutoGap", description = "Eats Golden Apples when your health is getting low", category = Category.COMBAT)
public final class AutoGap extends Module {

    private int ticks;
    public static int gap = -37;

    private final NumberSetting health = new NumberSetting("Health", this, 10, 0, 20, 0.1);

    private final BooleanSetting switchSlot = new BooleanSetting("Switch Slots", this, true);

    @Override
    public void onDisable() {
        gap = -37;
    }

    @Override
    public void onPreMotion(final PreMotionEvent event) {
        ++ticks;

        if (ticks > 10 && mc.thePlayer.getHealth() < health.getValue()) {
            gap = PlayerUtil.findGap() - 36;

            if (gap != -37) {
                mc.getNetHandler().addToSendQueueWithoutEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                if (switchSlot.isEnabled()) mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(gap));
                mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getStackInSlot(gap)));

                for (int i = 0; i <= 31; i++) mc.getNetHandler().addToSendQueue(new C03PacketPlayer());

                if (switchSlot.isEnabled())
                    mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));

                mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));

                ticks = 0;
            }
        }

        gap = -37;
    }


    @Override
    public void onPacketSend(final EventPacket event) {
        if (event.getPacket() instanceof C0APacketAnimation && gap != -37) {
            event.setCancelled(true);
            Client.INSTANCE.getNM().registerNotification("AutoGap");
        }
    }
}