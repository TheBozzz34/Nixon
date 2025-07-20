package xyz.necrozma.settings.impl;


import lombok.Getter;
import lombok.Setter;
import xyz.necrozma.module.Module;
import xyz.necrozma.settings.Settings;

@Getter
@Setter
public final class NoteSetting extends Settings {

    public NoteSetting(final String note, final Module parent) {
        this.name = note;
        parent.settings.add(this);
    }

}