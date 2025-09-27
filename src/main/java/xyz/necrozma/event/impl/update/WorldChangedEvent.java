package xyz.necrozma.event.impl.update;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.World;
import xyz.necrozma.event.Event;


@Getter
@Setter
@AllArgsConstructor
public class WorldChangedEvent extends Event {
    private World oldWorld, newWorld;
}