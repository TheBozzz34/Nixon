package xyz.necrozma.event.impl.render;

import xyz.necrozma.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiScreen;

@Getter
@Setter
@AllArgsConstructor
public class OpenGUIEvent extends Event {
    private final GuiScreen newScreen, oldScreen;
}
