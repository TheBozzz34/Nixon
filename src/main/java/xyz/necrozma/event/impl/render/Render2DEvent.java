package xyz.necrozma.event.impl.render;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.ScaledResolution;
import xyz.necrozma.event.Event;

@Getter
@Setter
@AllArgsConstructor
public final class Render2DEvent extends Event {
    private float partialTicks;
    private ScaledResolution scaledResolution;
}
