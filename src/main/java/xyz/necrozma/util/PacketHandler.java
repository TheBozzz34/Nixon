package xyz.necrozma.util;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Packet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.necrozma.Client;
import xyz.necrozma.event.impl.packet.EventPacket;

public class PacketHandler {

    private static final Logger logger = LogManager.getLogger();
    public void handlePacket(ChannelHandlerContext ctx, Packet msg) throws Exception {
        if(Client.INSTANCE.getMM() != null) {
            Client.INSTANCE.getMM().getModules().values().forEach(m -> {
                EventPacket e = new EventPacket(msg);
                m.onPacketSend(e);
            });
        }
    }
}
