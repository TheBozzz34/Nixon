package xyz.necrozma.event.impl.render;

import xyz.necrozma.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class Render3DEvent extends Event {
    private final float partialTicks;
}
