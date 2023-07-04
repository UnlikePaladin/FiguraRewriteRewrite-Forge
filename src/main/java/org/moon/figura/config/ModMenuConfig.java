package org.moon.figura.config;

import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;
import org.moon.figura.FiguraMod;
import org.moon.figura.gui.screens.ConfigScreen;

public class ModMenuConfig {

    public static void registerConfigScreen() {
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory(
                (client, parent) -> new ConfigScreen(parent, FiguraMod.DEBUG_MODE)));
    }
}