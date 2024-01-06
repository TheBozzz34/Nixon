package xyz.necrozma.util;

import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;

@UtilityClass
public final class PacketUtil {
    private final Minecraft mc = Minecraft.getMinecraft();

    public void sendPacket(final Packet<?> packet) {
        //mc.getNetHandler().addToSendQueue(packet);
        mc.thePlayer.sendQueue.addToSendQueue(packet);
    }
}
