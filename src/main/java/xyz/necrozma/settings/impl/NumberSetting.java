package xyz.necrozma.settings.impl;

import lombok.Getter;
import lombok.Setter;
import xyz.necrozma.module.Module;
import xyz.necrozma.settings.Settings;

import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public final class NumberSetting extends Settings {
    public double value, minimum, maximum, increment;
    public double renderPercentage, percentage;
    List<String> replacements;

    public NumberSetting(final String name, final Module parent, final double value, final double minimum, final double maximum, final double increment) {
        this.name = name;
        parent.settings.add(this);
        this.value = value;
        this.minimum = minimum;
        this.maximum = maximum;
        this.increment = increment;
    }

    public NumberSetting(final String name, final Module parent, final double value, final double minimum, final double maximum, final double increment, final String... replacements) {
        this.name = name;
        parent.settings.add(this);
        this.value = value;
        this.minimum = minimum;
        this.maximum = maximum;
        this.increment = increment;
        this.replacements = Arrays.asList(replacements);
    }
}