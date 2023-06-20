package org.moon.figura;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.enums.Opcode;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.framing.ControlFrame;
import org.java_websocket.handshake.HandshakedataImpl1;
import org.luaj.vm2.*;
import org.luaj.vm2.compiler.Constants;
import org.luaj.vm2.lib.*;
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
import org.moon.figura.permissions.Permissions;
import org.moon.figura.resources.FiguraRuntimeResources;
import org.moon.figura.wizards.AvatarWizard;

import java.nio.ByteBuffer;

@Mod.EventBusSubscriber(modid = FiguraMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class FiguraModClient {

    static {
       // ControlFrame.get(Opcode.PONG);
       // Draft.readLine(ByteBuffer.allocateDirect(1));
       // FiguraMod.LOGGER.info("Hello from UnlikePaladin");
     //   FiguraMod.LOGGER.info(WebSocketClient.class.getName());
        Class.forName(ControlFrame.class.getModule(), ControlFrame.class.getSimpleName());
        Class.forName(Draft.class.getModule(), Draft.class.getSimpleName());
        Class.forName(WebSocketClient.class.getModule(), WebSocketClient.class.getSimpleName());
        Class.forName(InvalidDataException.class.getModule(), InvalidDataException.class.getSimpleName());
        Class.forName(HandshakedataImpl1.class.getModule(), HandshakedataImpl1.class.getSimpleName());
        Class.forName(Permissions.class.getModule(), Permissions.class.getSimpleName());
        Class.forName(LibFunction.class.getModule(), LibFunction.class.getSimpleName());
        Class.forName(VarArgFunction.class.getModule(), VarArgFunction.class.getSimpleName());
        Class.forName(TwoArgFunction.class.getModule(), TwoArgFunction.class.getSimpleName());
        Class.forName(ZeroArgFunction.class.getModule(), ZeroArgFunction.class.getSimpleName());
        Class.forName(BaseLib.class.getModule(), BaseLib.class.getSimpleName());
        Class.forName(MathLib.class.getModule(), MathLib.class.getSimpleName());
        Class.forName(Varargs.class.getModule(), Varargs.class.getSimpleName());
        Class.forName(LuaTable.class.getModule(), LuaTable.class.getSimpleName());
        Class.forName(Globals.class.getModule(), Globals.class.getSimpleName());
        Class.forName(OneArgFunction.class.getModule(), OneArgFunction.class.getSimpleName());
        Class.forName(LuaNumber.class.getModule(), LuaNumber.class.getSimpleName());
        Class.forName(Constants.class.getModule(), Constants.class.getSimpleName());
        Class.forName(Lua.class.getModule(), Lua.class.getSimpleName());
        Class.forName(Lua.class.getModule(), Lua.class.getSimpleName());
    }

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
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("figura_overlay", new GUIOverlay());
        event.registerBelowAll("action_wheel_overlay", new GUIActionWheelOverlay());
    }
    @SubscribeEvent
    public static void registerKeyBinding(RegisterKeyMappingsEvent event) {
        for (KeyMapping value : Configs.KEY_MAPPINGS) {
            if(value != null)
                event.register(value);
        }
    }
}
