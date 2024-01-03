package xyz.necrozma.event.impl.packet;

import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.Getter;
import net.minecraft.network.Packet;
import xyz.necrozma.event.Event;

@Getter
@Setter
@AllArgsConstructor
public final class EventPacket extends Event {
    private Packet<?> packet;

}
