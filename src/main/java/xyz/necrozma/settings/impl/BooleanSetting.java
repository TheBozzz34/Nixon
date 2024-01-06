package xyz.necrozma.settings.impl;

import lombok.Getter;
import lombok.Setter;
import xyz.necrozma.module.Module;
import xyz.necrozma.settings.Settings;

@Getter
@Setter
public final class BooleanSetting extends Settings {

    public boolean enabled;

    public BooleanSetting(final String name, final Module parent, final boolean enabled) {
        this.name = name;
        parent.settings.add(this);
        this.enabled = enabled;
    }

    public void toggle() {
        enabled = !enabled;
    }

}