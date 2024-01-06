package xyz.necrozma.settings;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Settings {
    public String name;
    public boolean hidden;
}
