package xyz.necrozma.settings.impl;



import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Setter;
import xyz.necrozma.settings.Settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ModeSetting extends Settings {

    public final List<String> modes;
    private final HashMap<String, ArrayList<Settings>> childrenMap = new HashMap<>();
    private String defaultMode;
    private int modeIndex;

    @Setter
    @Expose
    @SerializedName("value")
    private String currentMode;

    public ModeSetting(String name, String defaultMode, String... modes) {
        this.name = name;
        this.modes = Arrays.asList(modes);
        this.modeIndex = this.modes.indexOf(defaultMode);
        if (currentMode == null) currentMode = defaultMode;
    }


    public String getMode() {
        return currentMode;
    }

    public boolean is(String mode) {
        return currentMode.equalsIgnoreCase(mode);
    }

    public void cycleForwards() {
        modeIndex++;
        if (modeIndex > modes.size() - 1) modeIndex = 0;
        currentMode = modes.get(modeIndex);
    }

    public void cycleBackwards() {
        modeIndex--;
        if (modeIndex < 0) modeIndex = modes.size() - 1;
        currentMode = modes.get(modeIndex);
    }


    public String getConfigValue() {
        return currentMode;
    }

}