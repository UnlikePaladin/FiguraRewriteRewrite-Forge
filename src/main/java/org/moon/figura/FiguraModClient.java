package org.moon.figura;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.gui.OverlayRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.enums.Opcode;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.framing.ControlFrame;
import org.java_websocket.handshake.HandshakedataImpl1;
import org.java_websocket.handshake.ServerHandshake;
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

import java.net.URI;
import java.nio.ByteBuffer;

@Mod.EventBusSubscriber(modid = FiguraMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class FiguraModClient {

    static {
        ControlFrame.get(Opcode.PONG);
        Draft.readLine(ByteBuffer.allocateDirect(1));

        /*
          This was an attempt at mitigating log spam outside of a dev environment
          Caused by this bug https://github.com/MinecraftForge/EventBus/issues/44
         */
        try {
            WebSocketClient webSocketClient = new WebSocketClient(new URI("")) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {

                }

                @Override
                public void onMessage(String message) {

                }

                @Override
                public void onClose(int code, String reason, boolean remote) {

                }

                @Override
                public void onError(Exception ex) {

                }
            };
            webSocketClient.close();
            new HandshakedataImpl1();
            Permissions.COMPLEXITY.showSteps();
            LibFunction.NONE.checkint();
            VarArgFunction.NONE.checkint();
            TwoArgFunction.NONE.checkint();
            OneArgFunction.NONE.checkint();
            ZeroArgFunction.NONE.checkint();
            BaseLib.NONE.checkint();
            MathLib.NONE.checkint();
            new Varargs() {
                @Override
                public LuaValue arg(int i) {
                    return null;
                }

                @Override
                public int narg() {
                    return 0;
                }

                @Override
                public LuaValue arg1() {
                    return null;
                }

                @Override
                public Varargs subargs(int start) {
                    return null;
                }
            }.arg1();
            LuaTable.NONE.call();
            Globals.NONE.call();
            OneArgFunction.NONE.call();
            LuaNumber.NONE.checkint();
            Constants.GET_OPCODE(Lua.iABC);
            Lua.GETARG_A(0);
        } catch (Exception ignored) {}

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
