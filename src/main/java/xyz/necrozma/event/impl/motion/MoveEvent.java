package xyz.necrozma.event.impl.motion;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import xyz.necrozma.event.Event;

@Getter
@Setter
@AllArgsConstructor
public final class MoveEvent extends Event {
    private double x, y, z;
}