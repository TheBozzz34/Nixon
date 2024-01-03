package xyz.necrozma.event.impl.input;
import lombok.AllArgsConstructor;
import lombok.Getter;

import xyz.necrozma.event.Event;
@Getter
@AllArgsConstructor
public final class EventKey extends Event {
    private final int key;
}
