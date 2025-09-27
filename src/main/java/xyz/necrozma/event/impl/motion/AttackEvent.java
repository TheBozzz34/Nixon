package xyz.necrozma.event.impl.motion;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.Entity;
import xyz.necrozma.event.Event;

@Getter
@Setter
@AllArgsConstructor
public final class AttackEvent extends Event {
    public Entity target;
}