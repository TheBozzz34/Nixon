package xyz.necrozma.util;

import net.minecraft.client.Minecraft;
import xyz.necrozma.Client;
import xyz.necrozma.gui.font.CustomFont;
import xyz.necrozma.gui.font.TTFFontRenderer;
import xyz.necrozma.module.Module;
import xyz.necrozma.module.ModuleManager;

public interface InstanceAccess {
    Minecraft mc = Minecraft.getMinecraft();

    ModuleManager moduleManager = Client.INSTANCE.getMM();

    TTFFontRenderer fontRenderer = CustomFont.FONT_MANAGER.getFont("Light 18");
    TTFFontRenderer fontRendererMedium = CustomFont.FONT_MANAGER.getFont("Light 24");
    TTFFontRenderer fontRendererBig = CustomFont.FONT_MANAGER.getFont("Light 36");
    TTFFontRenderer fontRendererHuge = CustomFont.FONT_MANAGER.getFont("Light 72");

    TTFFontRenderer fontRendererBold = CustomFont.FONT_MANAGER.getFont("Light 18");

    TTFFontRenderer altoSmall = CustomFont.FONT_MANAGER.getFont("Biko 18");
    TTFFontRenderer altoCock = CustomFont.FONT_MANAGER.getFont("Biko 28");
    TTFFontRenderer alto = CustomFont.FONT_MANAGER.getFont("Biko 36");
    TTFFontRenderer altoHuge = CustomFont.FONT_MANAGER.getFont("Biko 48");

    TTFFontRenderer comfortaa = CustomFont.FONT_MANAGER.getFont("Comfortaa 18");
    TTFFontRenderer comfortaaNigger = CustomFont.FONT_MANAGER.getFont("Comfortaa 26");
    TTFFontRenderer comfortaaBig = CustomFont.FONT_MANAGER.getFont("Comfortaa 32");
    TTFFontRenderer skidFont = CustomFont.FONT_MANAGER.getFont("Skid 24");
    TTFFontRenderer skidFontBig = CustomFont.FONT_MANAGER.getFont("Skid 48");
    TTFFontRenderer skeet = CustomFont.FONT_MANAGER.getFont("SkeetBold 12");
    TTFFontRenderer skeetBig = CustomFont.FONT_MANAGER.getFont("Skeet 18");
    TTFFontRenderer oneTap = CustomFont.FONT_MANAGER.getFont("Skeet 16");

    TTFFontRenderer museo = CustomFont.FONT_MANAGER.getFont("Museo 20");
    TTFFontRenderer eaves = CustomFont.FONT_MANAGER.getFont("Eaves 18");


    default Module getModule(final Class<? extends Module> clazz) {
        for (final Module module : Client.INSTANCE.getMM().getModules().values()) {
            if (module.getClass() == clazz) {
                return module;
            }
        }

        return null;
    }
}
