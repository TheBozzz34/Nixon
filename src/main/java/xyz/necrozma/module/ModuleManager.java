package xyz.necrozma.module;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;

import java.util.HashMap;
import java.util.Set;

@Getter
public final class ModuleManager {
    private final HashMap<Class<? extends  Module>, Module> modules;

    private static final Logger logger = LogManager.getLogger();

    public ModuleManager() {
        this.modules = new HashMap<>();
        register();
    }

    public final Module getModule(Class<? extends Module> mod) {
        return modules.get(mod);
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
}