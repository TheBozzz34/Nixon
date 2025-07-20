package xyz.necrozma.module;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;
import xyz.necrozma.settings.Settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Getter
public final class ModuleManager {
    private final HashMap<Class<? extends  Module>, Module> modules;

    private String getSettingName, getSettingSettingName;
    private Settings getSettingSetting;

    private static final Logger logger = LogManager.getLogger();

    public ModuleManager() {
        this.modules = new HashMap<>();
        register();
    }

    public final Module getModule(Class<? extends Module> mod) {
        return modules.get(mod);
    }

    public List<Module> getModulesByCategory(final Category category) {
        final List<Module> modulesByCategory = new ArrayList<>();

        for (final Module module : modules.values()) {
            if (module.getCategory() == category) {
                modulesByCategory.add(module);
            }
        }

        return modulesByCategory;
    }

    public final Module getModuleFromString(String name) {
        for (Module module : modules.values()) {
            if (module.getName().equalsIgnoreCase(name)) {
                return module;
            }
        }
        return null;
    }

    public void register() {
        final Reflections refl = new Reflections("xyz.necrozma.module.impl");

        final Set<Class<? extends Module>> classes = refl.getSubTypesOf(Module.class);

        for (Class<? extends Module> aClass : classes) {
            try {
                final Module module = aClass.newInstance();
                modules.put(aClass, module);
            } catch (InstantiationException | IllegalAccessException ignored) {
            }
        }
    }

    public void unRegister(Module... module) {
        for (Module mod : module) {
            modules.remove(mod.getClass());
        }
    }

    public Settings getSetting(final String moduleName, final String settingName) {
        if (getSettingName != null && getSettingSettingName != null && getSettingSetting != null) {
            if (getSettingName.equals(moduleName) && getSettingSettingName.equals(settingName)) {
                return getSettingSetting;
            }
        }

        for (final Module m : modules.values()) {
            if (m.getName().equalsIgnoreCase(moduleName)) {
                for (final Settings s : m.getSettings()) {
                    if (s.getName().equalsIgnoreCase(settingName)) {
                        getSettingName = moduleName;
                        getSettingSettingName = settingName;
                        getSettingSetting = s;

                        return s;
                    }
                }

            }
        }

        return null;
    }
}