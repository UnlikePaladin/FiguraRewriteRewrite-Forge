package org.moon.figura.config;

import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.fml.ModLoadingContext;
import org.moon.figura.FiguraMod;
import org.moon.figura.gui.screens.ConfigScreen;

public class ModMenuConfig {

    public static void registerConfigScreen() {
        ModLoadingContext.get().registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class, () -> new ConfigGuiHandler.ConfigGuiFactory(
                (client, parent) -> new ConfigScreen(parent, FiguraMod.DEBUG_MODE)));
    }
}