package xyz.necrozma.event.impl.packet;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.Packet;
import xyz.necrozma.event.Event;

@Getter
@Setter
@AllArgsConstructor
public final class PacketReceiveEvent extends Event {
    private Packet packet;
}