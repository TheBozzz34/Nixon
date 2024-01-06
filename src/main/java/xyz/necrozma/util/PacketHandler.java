package xyz.necrozma.util;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Packet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PacketHandler {

    private static final Logger logger = LogManager.getLogger();
    public void handlePacket(ChannelHandlerContext ctx, Packet msg) throws Exception {
        // Packet hooks go here
    }
}
