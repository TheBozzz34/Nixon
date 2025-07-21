package xyz.necrozma.module.impl.render;

/*
 Copyright Alan Wood 2021
 None of this code to be reused without my written permission
 Intellectual Rights owned by Alan Wood
 */

import xyz.necrozma.event.impl.update.EventUpdate;
import xyz.necrozma.module.Category;
import xyz.necrozma.module.Module;
import xyz.necrozma.module.ModuleInfo;

@ModuleInfo(name = "NoWeather", description = "Removes weather", category = Category.RENDER)
public final class NoWeather extends Module {

    //TODO: cant reenable this module after disabling it, need to fix
    @Override
    public void onUpdate(final EventUpdate event) {
        mc.theWorld.setThunderStrength(0);
        mc.theWorld.setRainStrength(0);
    }
}
