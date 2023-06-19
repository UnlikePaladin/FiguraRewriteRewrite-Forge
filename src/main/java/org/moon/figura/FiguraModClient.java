package org.moon.figura;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.gui.OverlayRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.avatar.local.CacheAvatarLoader;
import org.moon.figura.avatar.local.LocalAvatarFetcher;
import org.moon.figura.avatar.local.LocalAvatarLoader;
import org.moon.figura.config.ConfigManager;
import org.moon.figura.config.Configs;
import org.moon.figura.config.ModMenuConfig;
import org.moon.figura.entries.EntryPointManager;
import org.moon.figura.forge.GUIActionWheelOverlay;
import org.moon.figura.forge.GUIOverlay;
import org.moon.figura.gui.Emojis;
import org.moon.figura.lua.FiguraAPIManager;
import org.moon.figura.lua.docs.FiguraDocsManager;
import org.moon.figura.permissions.PermissionManager;
import org.moon.figura.resources.FiguraRuntimeResources;
import org.moon.figura.wizards.AvatarWizard;

@Mod.EventBusSubscriber(modid = FiguraMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class FiguraModClient {

    @SubscribeEvent
    public static void onInitializeClient(FMLClientSetupEvent event) {
        //init managers
        EntryPointManager.init();
        ConfigManager.init();
        PermissionManager.init();
        LocalAvatarFetcher.init();
        CacheAvatarLoader.init();
        FiguraDocsManager.init();
        FiguraRuntimeResources.init();
        ModMenuConfig.registerConfigScreen();
    }

    @SubscribeEvent
    public static void registerResourceListener(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(LocalAvatarLoader.AVATAR_LISTENER);
        event.registerReloadListener(Emojis.RESOURCE_LISTENER);
        event.registerReloadListener(AvatarWizard.RESOURCE_LISTENER);
        event.registerReloadListener(AvatarManager.RESOURCE_RELOAD_EVENT);
    }

    @SubscribeEvent
    public static void registerOverlays(FMLClientSetupEvent event) {
        OverlayRegistry.registerOverlayTop("figura_overlay", new GUIOverlay());
        OverlayRegistry.registerOverlayBottom("action_wheel_overlay", new GUIActionWheelOverlay());
    }
    @SubscribeEvent
    public static void registerKeyBinding(FMLClientSetupEvent event) {
        for (KeyMapping keyBinding : Configs.KEY_MAPPINGS) {
            if(keyBinding != null)
                ClientRegistry.registerKeyBinding(keyBinding);
        }
    }
}
