package org.moon.figura;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.avatar.local.CacheAvatarLoader;
import org.moon.figura.avatar.local.LocalAvatarFetcher;
import org.moon.figura.avatar.local.LocalAvatarLoader;
import org.moon.figura.backend2.NetworkStuff;
import org.moon.figura.commands.FiguraCommands;
import org.moon.figura.config.Config;
import org.moon.figura.config.ConfigManager;
import org.moon.figura.gui.ActionWheel;
import org.moon.figura.config.ModMenuConfig;
import org.moon.figura.gui.Emojis;
import org.moon.figura.gui.PaperDoll;
import org.moon.figura.gui.PopupMenu;
import org.moon.figura.lua.FiguraAPIManager;
import org.moon.figura.lua.FiguraLuaPrinter;
import org.moon.figura.lua.docs.FiguraDocsManager;
import org.moon.figura.mixin.SkullBlockEntityAccessor;
import org.moon.figura.trust.TrustManager;
import org.moon.figura.utils.ColorUtils;
import org.moon.figura.utils.TextUtils;
import org.moon.figura.utils.Version;
import org.moon.figura.wizards.AvatarWizard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.UUID;

@Mod("figura")
public class FiguraMod {

    public static final String MOD_ID = "figura";
    public static final String MOD_NAME = "Figura";
    public static final Version VERSION = new Version(ModList.get().getModContainerById("figura").get().getModInfo().getVersion().toString());
    public static final boolean DEBUG_MODE = Math.random() + 1 < 0;
    public static final Calendar CALENDAR = Calendar.getInstance();
    public static final Path GAME_DIR = FMLPaths.GAMEDIR.relative().normalize();
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static int ticks = 0;
    public static Entity extendedPickEntity;
    public static Component splashText;
    public FiguraMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onInitializeClient);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerKeyBinding);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerResourceListener);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerOverlays);
    }
    public void onInitializeClient(FMLClientSetupEvent event) {
        //init managers
        ConfigManager.init();
        TrustManager.init();
        LocalAvatarFetcher.init();
        CacheAvatarLoader.init();
        FiguraAPIManager.init();
        FiguraDocsManager.init();
        FiguraCommands.init();
        ModMenuConfig.registerConfigScreen();
    }

    public void registerResourceListener(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(LocalAvatarLoader.AVATAR_LISTENER);
        event.registerReloadListener(Emojis.RESOURCE_LISTENER);
        event.registerReloadListener(AvatarWizard.RESOURCE_LISTENER);
    }

    @SubscribeEvent
    public void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("figura_overlay", new GUIOverlay());
        event.registerBelowAll("action_wheel_overlay", new GUIActionWheelOverlay());
    }
    @SubscribeEvent
    public void registerKeyBinding(RegisterKeyMappingsEvent event) {
        for (Config value : Config.values()) {
            if(value.keyBind != null)
                event.register(value.keyBind);
        }
    }
    public static void tick() {
        pushProfiler("network");
        NetworkStuff.tick();
        popPushProfiler("files");
        LocalAvatarLoader.tickWatchedKey();
        popPushProfiler("avatars");
        AvatarManager.tickLoadedAvatars();
        popPushProfiler("chatPrint");
        FiguraLuaPrinter.printChatFromQueue();
        popProfiler();
        ticks++;
    }

    private static void renderFirstPersonWorldParts(WorldRenderContext context) {
        if (!context.camera().isDetached()) {
            Entity watcher = context.camera().getEntity();
            Avatar avatar = AvatarManager.getAvatar(watcher);
            if (avatar != null)
                avatar.firstPersonWorldRender(watcher, context.consumers(), context.matrixStack(), context.camera(), context.tickDelta());
        }
    }

    private static void hudRender(PoseStack stack, float delta) {
        if (AvatarManager.panic)
            return;

        pushProfiler(MOD_ID);

        pushProfiler("paperdoll");
        PaperDoll.render(stack);

        popPushProfiler("actionWheel");
        ActionWheel.render(stack);

        popPushProfiler("popupMenu");
        PopupMenu.render(stack);

        popProfiler(2);
    }

    private static void registerResourceListener(ResourceManagerHelper managerHelper) {
        managerHelper.registerReloadListener(LocalAvatarLoader.AVATAR_LISTENER);
        managerHelper.registerReloadListener(Emojis.RESOURCE_LISTENER);
        managerHelper.registerReloadListener(AvatarWizard.RESOURCE_LISTENER);
    }

    // -- Helper Functions -- //

    //debug print
    public static void debug(String str, Object... args) {
        if (DEBUG_MODE) LOGGER.info(str, args);
        else LOGGER.debug(str, args);
    }

    //mod root directory
    public static Path getFiguraDirectory() {
        String config = Config.MAIN_DIR.asString();
        Path p = config.isBlank() ? GAME_DIR.resolve(MOD_ID) : Path.of(config);
        try {
            Files.createDirectories(p);
        } catch (FileAlreadyExistsException ignored) {
        } catch (Exception e) {
            LOGGER.error("Failed to create the main " + MOD_NAME + " directory", e);
        }

        return p;
    }

    //mod cache directory
    public static Path getCacheDirectory() {
        Path p = getFiguraDirectory().resolve("cache");
        try {
            Files.createDirectories(p);
        } catch (FileAlreadyExistsException ignored) {
        } catch (Exception e) {
            LOGGER.error("Failed to create cache directory", e);
        }

        return p;
    }

    //get local player uuid
    public static UUID getLocalPlayerUUID() {
        return Minecraft.getInstance().getUser().getGameProfile().getId();
    }

    public static boolean isLocal(UUID other) {
        return getLocalPlayerUUID().equals(other);
    }

    /**
     * Sends a chat message right away. Use when you know your message is safe.
     * If your message is unsafe, (generated by a user), use luaSendChatMessage instead.
     * @param message - text to send
     */
    public static void sendChatMessage(Component message) {
        if (Minecraft.getInstance().gui != null)
            Minecraft.getInstance().gui.getChat().addMessage(TextUtils.replaceTabs(message));
        else
            LOGGER.info(message.getString());
    }

    /**
     * Converts a player name to UUID using minecraft internal functions.
     * @param playerName - the player name
     * @return - the player's uuid or null
     */
    public static UUID playerNameToUUID(String playerName) {
        GameProfileCache cache = SkullBlockEntityAccessor.getProfileCache();
        if (cache == null)
            return null;

        var profile = cache.get(playerName);
        return profile.isEmpty() ? null : profile.get().getId();
    }

    public static Style getAccentColor() {
        Avatar avatar = AvatarManager.getAvatarForPlayer(getLocalPlayerUUID());
        int color = avatar != null ? ColorUtils.rgbToInt(ColorUtils.userInputHex(avatar.color, ColorUtils.Colors.FRAN_PINK.vec)) : ColorUtils.Colors.FRAN_PINK.hex;
        return Style.EMPTY.withColor(color);
    }

    // -- profiler -- //

    public static void pushProfiler(String name) {
        Minecraft.getInstance().getProfiler().push(name);
    }

    public static void pushProfiler(Avatar avatar) {
        Minecraft.getInstance().getProfiler().push(avatar.entityName.isBlank() ? avatar.owner.toString() : avatar.entityName);
    }

    public static void popPushProfiler(String name) {
        Minecraft.getInstance().getProfiler().popPush(name);
    }

    public static void popProfiler() {
        Minecraft.getInstance().getProfiler().pop();
    }

    public static void popProfiler(int times) {
        var profiler = Minecraft.getInstance().getProfiler();
        for (int i = 0; i < times; i++)
            profiler.pop();
    }
}
